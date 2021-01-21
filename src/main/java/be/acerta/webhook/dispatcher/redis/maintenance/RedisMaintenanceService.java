package be.acerta.webhook.dispatcher.redis.maintenance;

import be.acerta.webhook.dispatcher.redis.dto.v1.RedisInfoDtoV1;

/**
 * Utility service voor maintenance redenen.
 * Requests vanuit - bv postman - voor monitoring & maintenance;
 * bv om manueel messages uit bucket te removen etc
 */
public interface RedisMaintenanceService {

    void clear(BatchType batchType);

    RedisInfoDtoV1 getRedisInfo();

    void triggerProcessing(String groupId, String bucketId);

    void forceProcessing(String groupId, String bucketId);

    boolean isProcessorLocked(String groupId, String bucketId);

    void removeEvent(String groupId, String bucketId, Long eventId);

    void removeBucket(String groupId, String bucketId);
    
}
