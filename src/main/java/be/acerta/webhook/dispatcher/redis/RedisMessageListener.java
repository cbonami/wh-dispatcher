package be.acerta.webhook.dispatcher.redis;

import static be.acerta.webhook.dispatcher.LazyString.lazy;
import static be.acerta.webhook.dispatcher.redis.RedisClient.NEXT_RETRY_INTERVAL_MULTIPLY_FACTOR;
import static be.acerta.webhook.dispatcher.redis.RedisClient.UNASSIGNED_KEY;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.NoSuchElementException;
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

    private List<MessageProcessingStrategy> processingStrategies;
    private final String id;
    private RedisClient client;

    protected RedisMessageListener(RedisClient client, List<MessageProcessingStrategy> eventStrategies) {
        this.processingStrategies = eventStrategies;
        this.id = randomUUID().toString();
        this.client = client;
        unassignExistingListeners();
    }

    public void startListeners() {

        // processorlock caches (zie producer)
        // listener en lock systeem zit hier
        // groupId is unieke naam voor unieke integratie-stroom die door deze dispatcher
        // service wordt bediend; bv 'appX_2_appY'
        log.info("--- Adding listeners for group id {} ---", client.groupId());
        client.getProcessors().addListener((EntryCreatedListener<String, String>) e -> processMessage(e.getKey()));
        client.getProcessors().addListener((EntryUpdatedListener<String, String>) e -> processMessage(e.getKey()));

        // 3e cache verantwoordelijk voor retries
        // key = bucket in cache en we zetten daar een expiry op van 2, volgende keer 4,
        // dan 8
        // haalt message uit map
        // exception ==> message blijft in map
        // ==> key in waitretry met expiry van 2 sec
        // op gegeven moment stop je wel met retryen en dan komt het op de watchdog
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
                    log.debug("Processing message id {} for bucket id {}", firstMessageId, bucketId);
                    determineMessageType(msg).ifPresentOrElse(messageType -> {
                        log.debug("Determined event type {} for message id {} and bucket id {}", messageType,
                                firstMessageId, bucketId);

                        // strategy pattern om te zien wat er moet gebeuren
                        // messageType bepaalt gekozen strategy
                        determineStrategy(messageType).ifPresentOrElse(messageStrategy -> {
                            log.debug("Determined message strategy {} for message id {} and bucket id {}",
                                    messageStrategy, firstMessageId, bucketId);
                            messageStrategy.processMessage(msg);
                        }, () -> {
                            throw new NoSuchElementException(String.format(
                                    "No strategy could be found for messageType in msg %s", lazy(msg::toString)));
                        });
                    }, () -> {
                        throw new NoSuchElementException(String.format(
                                "No known messageType could be distilled from msg %s", lazy(msg::toString)));
                    });
                    client.removeMessage(bucketId, msg);
                } catch (NoSuchElementException ex) {
                    // if messages were sent that we cannot process, we simply disregard that and continue
                    throw ex;
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
                log.error(format("Process message id %s for bucket id %s has failed", actualMessageId, bucketId), ex);
            } else {
                log.error(format("Process message for bucket id %s has failed, message id not found", bucketId), ex);
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

    private RLock awaitRetryAndReleaseProcessor(String bucketId, String msg, Exception ex) {
        log.error(format("Processing of message has failed; message=[%s]", msg), ex);
        RLock awaitRetryLock = client.getAwaitRetryLock(bucketId);
        // semafoor, omdat we vanalles moeten berekenen hieronder
        awaitRetryLock.lock();

        // get next retry interval value and increment
        Integer nextRetryInterval = client.getNextRetryIntervals().get(bucketId);
        if (nextRetryInterval == null || nextRetryInterval == 0) {
            nextRetryInterval = NEXT_RETRY_INTERVAL_MULTIPLY_FACTOR;
        }
        if (log.isErrorEnabled())
            log.error(format("Processing of message [%s] has failed. Next attempt in %s secs", msg, nextRetryInterval),
                    ex);
        client.getAwaitRetries().fastPut(bucketId, nextRetryInterval, nextRetryInterval, SECONDS);

        if (isSmallerThanRedisMaxRetryInterval(nextRetryInterval)) {
            client.getNextRetryIntervals().put(bucketId, nextRetryInterval * NEXT_RETRY_INTERVAL_MULTIPLY_FACTOR);
        } else {
            client.getNextRetryIntervals().put(bucketId, NEXT_RETRY_INTERVAL_MULTIPLY_FACTOR);
        }

        // un-assign processor
        client.getProcessors().fastPut(bucketId, UNASSIGNED_KEY);
        return awaitRetryLock;
    }

    boolean isSmallerThanRedisMaxRetryInterval(Integer nextRetryInterval) {
        return nextRetryInterval < getRedisMaxRetryInterval();
    }

    private Optional<MessageProcessingStrategy> determineStrategy(MessageType eventType) {
        return processingStrategies.stream().filter(eventStrategy -> eventStrategy.canProcess(eventType)).findFirst();
    }

    private void unassignExistingListeners() {
        // look for existing listeners and activate
        log.info("--- Unassigning existing listeners on buckets ---");
        client.getProcessors().entrySet().stream().filter(entry -> !entry.getValue().equals(UNASSIGNED_KEY))
                .peek(entry -> log.info("--- Found listener to unassign with id {} on bucket {} ", entry.getKey(),
                        entry.getValue()))
                .collect(toSet()).iterator().forEachRemaining(entry -> entry.setValue(UNASSIGNED_KEY));
    }


    private String determineFirstMessageId(String bucketId) {
        Optional<String> message = client.getBuckets().get(bucketId).stream().findFirst();
        return message.map(m-> new JSONObject(m).get("id").toString()).orElse(null);
    }

    protected abstract Optional<MessageType> determineMessageType(String msg);

    protected abstract Long getRedisMaxRetryInterval();
}
