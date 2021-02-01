package be.acerta.webhook.dispatcher.redis.webhook;

import be.acerta.webhook.dispatcher.redis.RedisClient;
import org.redisson.api.RedissonClient;

public class WebhookRedisClient extends RedisClient {

    public static final String WEBHOOK_REDIS_GROUP = "webhookRedisGroup";

    private final String groupId;

    public WebhookRedisClient(RedissonClient client) {
        super(client);
        this.groupId = WEBHOOK_REDIS_GROUP;
    }

    @Override
    public String groupId() {
        return this.groupId;
    }

}
