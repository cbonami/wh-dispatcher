package be.acerta.webhook.dispatcher.redis;

import static be.acerta.webhook.dispatcher.redis.RedisClient.UNASSIGNED_KEY;
import java.util.Optional;
import org.redisson.api.RLock;

public class RedisProcessor {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RedisProcessor.class);
    private final RedisClient client;
    private RLock processorLock;

    public RedisProcessor(RedisClient client) {
        this.client = client;
    }

    protected Optional<String> getFirstMessageIfProcessable(String bucketId, String processorId) {
        if (canProcess(bucketId)) {
            client.getProcessors().fastPut(bucketId, processorId);
            return client.getBuckets().get(bucketId).stream().findFirst();
        }
        return Optional.empty();
    }

    void finishProcessing(String bucketId, String processorId) {
        if (processorLock != null && processorLock.isHeldByCurrentThread()) {
            String processor = client.getProcessors().get(bucketId);
            if (processor != null && processor.equals(processorId)) {
                client.getProcessors().fastPut(bucketId, UNASSIGNED_KEY);
            }
            processorLock.unlock();
        }
    }

    // als hij leeg is kan je hem niet processen, als hij locked is ook niet enz
    // eerste die aan bucket wint, mag beginnen
    private boolean canProcess(String bucketId) {
        if (client.getBuckets().get(bucketId).isEmpty()) {
            log.debug("Cannot process message for bucket id {} because bucket is empty", bucketId);
            return false;
        }
        processorLock = client.getProcessorLock(bucketId);
        if (!processorLock.tryLock()) {
            log.debug("Cannot process message for bucket id {} because cannot get lock on processor", bucketId);
            return false;
        }
        // herhaling had een reden !
        // niet gelockt maar ondertussen wel leeg
        if (client.getBuckets().get(bucketId).isEmpty()) {
            log.debug("Cannot process message for bucket id {} because bucket is empty", bucketId);
            return false;
        }
        // UNASSIGNED_KEY is allemaal nullekes
        // processor die de unassigned kan omzitten in een echte UUID, wint
        if (!client.getProcessors().get(bucketId).equals(UNASSIGNED_KEY)) {
            log.debug("Cannot process message for bucket id {} because other processor has been assigned", bucketId);
            return false;
        }
        // als de bucket al zat te wachten op een retry, en er komt ondertussen een
        // message bij in de bucket
        // ==> niks doen en het overlaten aan de geschedulede processor die binnen
        // enkele sec afgaat
        Integer awaitRetryInterval = client.getAwaitRetries().get(bucketId);
        if (!(awaitRetryInterval == null || awaitRetryInterval == 0)) {
            log.debug("Cannot process message for bucket id {} because the bucket is waiting for a retry", bucketId);
        }
        return awaitRetryInterval == null || awaitRetryInterval == 0;
    }
}
