package be.acerta.webhook.dispatcher.redis.webhook;

import be.acerta.webhook.dispatcher.redis.RedisClient;
import org.redisson.api.RedissonClient;

public class WebhookRedisClient extends RedisClient {

    private final String groupId;

    public WebhookRedisClient(RedissonClient client) {
        super(client);
        this.groupId = "webhookRedisGroup";
    }

    @Override
    public String groupId() {
        return this.groupId;
    }

}
