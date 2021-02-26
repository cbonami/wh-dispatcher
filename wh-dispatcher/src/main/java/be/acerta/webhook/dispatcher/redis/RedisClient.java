package be.acerta.webhook.dispatcher.redis;

import static be.acerta.webhook.dispatcher.LazyString.lazy;
import static java.util.Objects.isNull;
import java.util.UUID;
import org.json.JSONObject;
import org.redisson.api.RLock;
import org.redisson.api.RMapCache;
import org.redisson.api.RMultimapCache;
import org.redisson.api.RedissonClient;

public abstract class RedisClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RedisClient.class);
    public static final String UNASSIGNED_KEY = new UUID(0, 0).toString();
    public static final String PROCESSOR_LOCK_SUFFIX = "processorlock";
    static final String AWAIT_RETRY_SUFFIX = "awaitretry";
    static final String NEXT_RETRY_INTERVAL = "nextretryinterval";
    public static final Integer NEXT_RETRY_INTERVAL_MULTIPLY_FACTOR = 2;
    public static final String LOCK_SUFFIX = "lock";
    public static final String SEPARATOR = "_";
    private final RedissonClient client;

    protected RedisClient(RedissonClient client) {
        this.client = client;
    }

    /**
     * Fetches a {@link java.util.Map} containing the intervals to wait before a
     * next retry is started for each bucket The interval is multiplied by 2 after
     * each failure.
     * <p>
     * Once the processing of the event is succesfull, the interval is reset to 2
     *
     * @return {@link RMapCache}
     */
    public RMapCache<String, Integer> getNextRetryIntervals() {
        return client.getMapCache(groupId() + SEPARATOR + NEXT_RETRY_INTERVAL);
    }

    /**
     * Fetches the multi-map containing the stored messages for this group
     *
     * @return {@link RMultimapCache}
     */
    public RMultimapCache<String, String> getBuckets() {
        return client.getListMultimapCache(groupId());
    }

    /**
     * Fetches a {@link java.util.Map} that contains a {@link UUID} for each bucket.
     * <p/>
     * When an event is published to Redis, a "zero" UUID is put as value for that
     * bucket. This triggers a Redisson listener so that processing of this message
     * is started.
     * <p/>
     * Once processing is able to start, a random UUID is put for that bucket so
     * other processors can see that this message is currently being processed
     *
     * @return {@link RMapCache}
     */
    public RMapCache<String, String> getProcessors() {
        return client.getMapCache(groupId() + SEPARATOR + PROCESSOR_LOCK_SUFFIX);
    }

    /**
     * Fetches the map containing the stored timeouts for this message group. The
     * value also equals the TimeToLive for this message. After TTL expires, message
     * will be evicted and {@link org.redisson.api.map.event.EntryExpiredListener}
     * will trigger next retry.
     * <p/>
     * When a bucket is present as key, it means that the processing of the first
     * message in this bucket has failed. The TTL that evicts the message is based
     * on the value in the {@link #getNextRetryIntervals()}
     *
     * @return {@link RMapCache}
     */
    public RMapCache<String, Integer> getAwaitRetries() {
        return client.getMapCache(groupId() + SEPARATOR + AWAIT_RETRY_SUFFIX);
    }

    /**
     * Gets a lock for the 'processors' map, used to synchronize access to the map
     * entry associated with the specified message bucket.
     *
     * @param bucketId Identifier of the message bucket
     * @return {@link RLock}
     */
    public RLock getProcessorLock(String bucketId) {
        return client.getLock(groupId() + SEPARATOR + PROCESSOR_LOCK_SUFFIX + SEPARATOR + LOCK_SUFFIX + SEPARATOR + bucketId);
    }

    /**
     * Gets a lock for the 'awaitRetry' map, used to synchronize access to the map
     * entry associated with the specified message bucket.
     *
     * @param bucketId Identifier of the message bucket
     * @return {@link RLock}
     */
    public RLock getAwaitRetryLock(String bucketId) {
        return client.getLock(groupId() + SEPARATOR + AWAIT_RETRY_SUFFIX + SEPARATOR + LOCK_SUFFIX + SEPARATOR + bucketId);
    }

    public static String getMessageId(String message) {
        return new JSONObject(message).get("id").toString();
    }

    /**
     * @deprecated the group-concept should be factored out; we will only use this
     *             service to dispatch webhooks, and for nothing else
     */
    @Deprecated
    public abstract String groupId();

    public void cleanRedis() {
        getProcessors().clear();
        getBuckets().clear();
        getAwaitRetries().clear();
        getNextRetryIntervals().clear();
    }

    public boolean hasEventBlockedForProcessing(String bucketId) {
        return (UNASSIGNED_KEY.equals(getProcessors().get(bucketId)) || isNull(getProcessors().get(bucketId))) && !isProcessorLocked(bucketId) && !getAwaitRetries().containsKey(bucketId);
    }

    public boolean isProcessorLocked(String bucketId) {
        return getProcessorLock(bucketId).isLocked();
    }

    public void removeBucket(String bucketId) {
        log.debug("Remove bucket {}", bucketId);
        getBuckets().removeAll(bucketId);
        removeBucketFromCaches(bucketId);
    }

    public void triggerProcessing(String bucketId) {
        log.debug("Trigger processing for bucket {}", bucketId);
        removeBucketFromRetryCaches(bucketId);
        getProcessors().fastPut(bucketId, UNASSIGNED_KEY);
    }

    public void forceProcessing(String bucketId) {
        log.debug("Force unlock en trigger processing voor bucket {}", bucketId);
        getProcessorLock(bucketId).forceUnlock();
        triggerProcessing(bucketId);
    }

    public void removeMessageById(String bucketId, String messageId) {
        log.debug("Verwijder voor event {} uit bucket {}", messageId, bucketId);
        String eventMessage = getBuckets().getAll(bucketId).stream().filter(message -> JsonUtil.getFieldAsString(message, "id").equals(messageId)).findFirst().orElse(null);
        removeMessage(bucketId, eventMessage);
        removeBucketFromCaches(bucketId);
    }

    /**
     * Removes a message from a bucket. This method is called when a mesagge is
     * succesfully processed or when a message holds an event that is not relevant
     *
     * @param bucketId
     * @param message
     */
    public void removeMessage(String bucketId, String message) {
        log.debug("Removing message {} with message id {} from bucket id {}", message, lazy(() -> getMessageId(message)), bucketId);
        getBuckets().remove(bucketId, message);
        if (getBuckets().get(bucketId).isEmpty()) {
            log.debug("Removing empty bucket with id {}", bucketId);
            getProcessors().remove(bucketId);
        }
        getNextRetryIntervals().remove(bucketId);
    }

    private void removeBucketFromCaches(String bucketId) {
        getProcessors().remove(bucketId);
        removeBucketFromRetryCaches(bucketId);
    }

    private void removeBucketFromRetryCaches(String bucketId) {
        getAwaitRetries().remove(bucketId);
        getNextRetryIntervals().remove(bucketId);
    }
}
