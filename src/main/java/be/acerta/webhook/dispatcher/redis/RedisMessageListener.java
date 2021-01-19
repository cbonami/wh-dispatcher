package be.acerta.webhook.dispatcher.redis;

import org.json.JSONObject;
import org.redisson.api.RLock;
import org.redisson.api.map.event.EntryCreatedListener;
import org.redisson.api.map.event.EntryExpiredListener;
import org.redisson.api.map.event.EntryUpdatedListener;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static be.acerta.webhook.dispatcher.redis.RedisClient.NEXT_RETRY_INTERVAL_MULTIPLY_FACTOR;
import static be.acerta.webhook.dispatcher.redis.RedisClient.UNASSIGNED_KEY;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class RedisMessageListener {

    private static final Logger LOGGER = getLogger(RedisMessageListener.class);

    private List<EventStrategy> eventStrategies;

    private final String id;
    private RedisClient client;

    protected RedisMessageListener(RedisClient client,
            List<EventStrategy> eventStrategies /* , ParameterService parameterService */) {
        this.eventStrategies = eventStrategies;
        this.id = randomUUID().toString();
        this.client = client;
        unassignExistingListeners();
    }

    public void startListeners() {

        // processorlock caches (zie producer)
        // listener en lock systeem zit hier
        // groupId is unieke naam bv 'projectbeheer_2_boeko'
        LOGGER.info("--- Adding listeners for group id {} ---", client.groupId());
        client.getProcessors().addListener((EntryCreatedListener<String, String>) e -> processMessage(e.getKey()));
        client.getProcessors().addListener((EntryUpdatedListener<String, String>) e -> processMessage(e.getKey()));

        // 3e cache verantwoordelijk voor retries
        // key = bucket in cache en we zetten daar een expiry op van 2, volgende keer 4,
        // dan 8
        // haalt message uit map
        // exception ==> message blijft in map
        // ==> key in waitretry met expiry van 2 sec
        // op gegeven moment stop je wel met rertyen en dan komt het op de watchdog
        // monitor
        client.getAwaitRetries().addListener((EntryExpiredListener<String, Integer>) e -> processRetry(e.getKey()));

        LOGGER.info("--- Start processing existing keys for group id {} ---", client.groupId());
        client.getBuckets().keySet().forEach(this::processMessage);
    }

    void processMessage(String bucketId) {
        RedisProcessor currentProcessor = new RedisProcessor(client);
        AtomicReference<String> messageId = new AtomicReference<>(null);
        try {
            currentProcessor.getFirstMessageIfProcessable(bucketId, id).ifPresent(msg -> {
                RLock awaitRetryLock = null;
                try {
                    String firstMessageId = determineFirstMessageId(bucketId);
                    messageId.set(firstMessageId);
                    LOGGER.info("Processing message id {} voor bucket id {}", firstMessageId, bucketId);
                    determineEventType(msg).ifPresent(eventType -> {
                        LOGGER.info("Determined event type {} voor message id {} en bucket id {}", eventType,
                                firstMessageId, bucketId);

                        // strategy pattern om te zien wat er moet gebeuren
                        // eventType bepaalt strategy
                        determineStrategy(eventType).ifPresent(eventStrategy -> {
                            LOGGER.info("Determined event strategy {} voor message id {} en bucket id {}",
                                    eventStrategy, firstMessageId, bucketId);
                            handleEvent(msg, eventStrategy);
                        });
                    });
                    client.removeMessage(bucketId, msg);
                } catch (Exception ex) {
                    // lock op await retry zodat niemand anders daarmee kan bezig zijn
                    // bereken interval
                    // we steken de bucket in de awaitretry cache
                    // we zetten terug een unassigned key op de processor cache om aan te geven dat
                    // deze processor niet meer bezig is
                    awaitRetryLock = awaitRetryAndReleaseProcessor(bucketId, msg, ex);
                    throw ex;
                } finally {
                    // de vorige lock die we op de awaitRetry hebben gelegd unlocken (zie
                    // awaitRetryAndReleaseProcessor)
                    // soms gaat er iets mis in redis en hier zorgen we ervoor dat de lock soweiso
                    // wordt gereleased
                    if (awaitRetryLock != null && awaitRetryLock.isHeldByCurrentThread()) {
                        awaitRetryLock.unlock();
                    }
                }
            });
        } catch (Exception ex) {
            String actualMessageId = messageId.get();
            if (actualMessageId != null) {
                LOGGER.error(format("Process message id %s voor bucket id %s is gefaald", actualMessageId, bucketId),
                        ex);
            } else {
                LOGGER.error(format("Process message voor bucket id %s is gefaald, message id niet gevonden", bucketId),
                        ex);
            }
        } finally {
            // zeker zijn dat de lock op processor cache wordt geunlocked
            // en moest het de vorige keer mislukt zijn, de unassigned key als value zetten
            currentProcessor.finishProcessing(bucketId, id);
        }
    }

    /**
     * Retry an event after it has been evicted from the awaitRetry cache
     * Effectively sets the bucket listener value to unassigned.
     *
     * @param bucketId Identifier of the message bucket
     */
    private void processRetry(String bucketId) {
        RLock awaitRetryLock = null;
        try {
            awaitRetryLock = client.getAwaitRetryLock(bucketId);
            if (!awaitRetryLock.tryLock()) {
                return;
            }

            RLock processorLock = client.getProcessorLock(bucketId);
            try {
                processorLock.lock();

                String processor = client.getProcessors().get(bucketId);
                if (processor == null || !processor.equals(UNASSIGNED_KEY)) {
                    return;
                }

                client.getProcessors().fastPut(bucketId, UNASSIGNED_KEY);
            } finally {
                if (processorLock.isHeldByCurrentThread()) {
                    processorLock.unlock();
                }
            }
        } catch (Exception ex) {
            LOGGER.error(format("Time-out processing voor bucketId[%s] is gefaald", bucketId), ex);
        } finally {
            if (awaitRetryLock != null && awaitRetryLock.isHeldByCurrentThread()) {
                awaitRetryLock.unlock();
            }
        }
    }

    private void handleEvent(String msg, EventStrategy eventStrategy) {
        //setVdabCallingApplication(getCurrentApplication());
        eventStrategy.handleEventMessageBody(msg, getCorrelationId(msg));
    }

    private RLock awaitRetryAndReleaseProcessor(String bucketId, String msg, Exception ex) {
        LOGGER.error(format("Afhandeling event met correlationId[%s] is gefaald", getCorrelationId(msg)), ex);
        RLock awaitRetryLock = client.getAwaitRetryLock(bucketId);
        // semafoor, omdat we vanalles moeten berekenen hieronder
        awaitRetryLock.lock();

        // get next retry interval value and increment
        Integer nextRetryInterval = client.getNextRetryIntervals().get(bucketId);
        if (nextRetryInterval == null || nextRetryInterval == 0) {
            nextRetryInterval = NEXT_RETRY_INTERVAL_MULTIPLY_FACTOR;
        }
        if (LOGGER.isErrorEnabled())
            LOGGER.error(format("Afhandeling event met correlationId[%s] is gefaald. Volgende poging in %s seconden",
                    getCorrelationId(msg), nextRetryInterval), ex);
        client.getAwaitRetries().fastPut(bucketId, nextRetryInterval, nextRetryInterval, SECONDS);

        if (isKleinerDanRedisMaxRetryInterval(nextRetryInterval)) {
            client.getNextRetryIntervals().put(bucketId, nextRetryInterval * NEXT_RETRY_INTERVAL_MULTIPLY_FACTOR);
        } else {
            client.getNextRetryIntervals().put(bucketId, NEXT_RETRY_INTERVAL_MULTIPLY_FACTOR);
        }

        // un-assign processor
        client.getProcessors().fastPut(bucketId, UNASSIGNED_KEY);
        return awaitRetryLock;
    }

    boolean isKleinerDanRedisMaxRetryInterval(Integer nextRetryInterval) {
        return nextRetryInterval < getRedisMaxRetryInterval();
    }

    private Optional<EventStrategy> determineStrategy(EventType eventType) {

        return eventStrategies.stream().filter(eventStrategy -> eventStrategy.canHandle(eventType)).findFirst();
    }

    private void unassignExistingListeners() {
        // look for existing listeners and activate
        LOGGER.info("--- Unassigning existing listeners on buckets for group id {} ---", client.groupId());
        client.getProcessors().entrySet().stream().filter(entry -> !entry.getValue().equals(UNASSIGNED_KEY))
                .peek(entry -> LOGGER.info("--- Found listener to unassign with id {} on bucket {} ", entry.getKey(),
                        entry.getValue()))
                .collect(toSet()).iterator().forEachRemaining(entry -> entry.setValue(UNASSIGNED_KEY));
    }

    private String getId(String message) {
        return new JSONObject(message).get("id").toString();
    }

    private String determineFirstMessageId(String bucketId) {
        Optional<String> message = client.getBuckets().get(bucketId).stream().findFirst();
        return message.map(this::getId).orElse(null);
    }

    protected abstract Optional<EventType> determineEventType(String msg);

    protected abstract String getCorrelationId(String msg);

    protected abstract Long getRedisMaxRetryInterval();
}
