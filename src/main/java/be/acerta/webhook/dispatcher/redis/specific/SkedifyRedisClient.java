package be.acerta.webhook.dispatcher.redis.specific;

import be.acerta.webhook.dispatcher.redis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

@Slf4j
public class SkedifyRedisClient extends RedisClient {

    private final String groupId;

    public SkedifyRedisClient(RedissonClient client) {
        super(client);
        this.groupId = "fooRedisGroup";
    }

    @Override
    public String groupId() {
        return this.groupId;
    }

}
