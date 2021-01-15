package be.acerta.webhook.dispatcher.redis;

import org.redisson.api.RMap;
import org.redisson.api.RMultimapCache;

import static be.acerta.webhook.dispatcher.redis.RedisClient.UNASSIGNED_KEY;

public abstract class RedisMessageProducer {
    
    private final RedisClient client;

    protected RedisMessageProducer(RedisClient client) {
        this.client = client;
    }

    public boolean publish(String bucketId, String message) {

        // main cache met key + lijst(messages) als value (typische multimap)
        // key = bucket
        RMultimapCache<String, String> messages = client.getBuckets();
        if (!messages.put(bucketId, message))
            return false;

        // 3 of 4 caches
        // key = bucket, value is UUID
        // als UUID allemaal nullekes, dan betekent dit dat er geen processor mee bezig is
        // wanneer processor in gang schiet dan pusht die bucket met UUID in processorlocks, zodanig dat een andere processor er niet gaat aankomen
        // aparte cache; reden: op de andere cache (hierboven) kan je geen locks leggen
        RMap<String, String> processorlocks = client.getProcessors();
        processorlocks.putIfAbsent(bucketId, UNASSIGNED_KEY);

        return true;
    }
}
