package be.acerta.webhook.dispatcher.redis;

import static be.acerta.webhook.dispatcher.redis.RedisClient.NEXT_RETRY_INTERVAL_MULTIPLY_FACTOR;
import static be.acerta.webhook.dispatcher.redis.RedisClient.UNASSIGNED_KEY;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.redisson.api.RLock;
import org.redisson.api.map.event.EntryCreatedListener;
import org.redisson.api.map.event.EntryExpiredListener;
import org.redisson.api.map.event.EntryUpdatedListener;

@Slf4j
public abstract class RedisMessageListener {

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
        // groupId is unieke naam voor unieke integratie-stroom die door deze dispatcher service wordt bediend; bv 'appX_2_appY'
        log.info("--- Adding listeners for group id {} ---", client.groupId());
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

        log.info("--- Start processing existing keys for group id {} ---", client.groupId());
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
                    log.info("Processing message id {} for bucket id {}", firstMessageId, bucketId);
                    determineEventType(msg).ifPresent(eventType -> {
                        log.info("Determined event type {} for message id {} and bucket id {}", eventType,
                                firstMessageId, bucketId);

                        // strategy pattern om te zien wat er moet gebeuren
                        // eventType bepaalt gekozen strategy
                        determineStrategy(eventType).ifPresent(eventStrategy -> {
                            log.info("Determined event strategy {} for message id {} and bucket id {}",
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
                log.error(format("Process message id %s for bucket id %s has failed", actualMessageId, bucketId),
                        ex);
            } else {
                log.error(format("Process message for bucket id %s has failed, message id not found", bucketId),
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
            log.error(format("Time-out processing for bucketId[%s] has failed", bucketId), ex);
        } finally {
            if (awaitRetryLock != null && awaitRetryLock.isHeldByCurrentThread()) {
                awaitRetryLock.unlock();
            }
        }
    }

    private void handleEvent(String msg, EventStrategy eventStrategy) {
        eventStrategy.handleEventMessageBody(msg, getCorrelationId(msg));
    }

    private RLock awaitRetryAndReleaseProcessor(String bucketId, String msg, Exception ex) {
        log.error(format("Processing of event with correlationId[%s] has failed", getCorrelationId(msg)), ex);
        RLock awaitRetryLock = client.getAwaitRetryLock(bucketId);
        // semafoor, omdat we vanalles moeten berekenen hieronder
        awaitRetryLock.lock();

        // get next retry interval value and increment
        Integer nextRetryInterval = client.getNextRetryIntervals().get(bucketId);
        if (nextRetryInterval == null || nextRetryInterval == 0) {
            nextRetryInterval = NEXT_RETRY_INTERVAL_MULTIPLY_FACTOR;
        }
        if (log.isErrorEnabled())
            log.error(format("Processing of event with correlationId[%s] has failed. Next attempt in %s secs",
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
        log.info("--- Unassigning existing listeners on buckets for group id {} ---", client.groupId());
        client.getProcessors().entrySet().stream().filter(entry -> !entry.getValue().equals(UNASSIGNED_KEY))
                .peek(entry -> log.info("--- Found listener to unassign with id {} on bucket {} ", entry.getKey(),
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
