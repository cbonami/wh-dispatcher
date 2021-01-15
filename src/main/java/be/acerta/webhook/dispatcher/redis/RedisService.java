package be.acerta.webhook.dispatcher.redis;

import be.acerta.webhook.dispatcher.redis.BatchType;
import be.acerta.webhook.dispatcher.redis.dto.v1.RedisInfoDtoV1;

// utility service
// calls vanuit postman voor monitoring
// manueel messages uit bucket removen etc
public interface RedisService {

    void clear(BatchType batchType);

    RedisInfoDtoV1 getRedisInfo();

    void triggerProcessing(String groupId, String bucketId);

    void forceProcessing(String groupId, String bucketId);

    boolean isProcessorLocked(String groupId, String bucketId);

    void removeEvent(String groupId, String bucketId, Long eventId);

    void removeBucket(String groupId, String bucketId);
}
