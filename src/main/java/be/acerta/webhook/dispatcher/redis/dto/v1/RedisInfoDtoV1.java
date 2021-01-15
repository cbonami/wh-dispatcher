package  be.acerta.webhook.dispatcher.redis.dto.v1;


import java.util.List;

import be.acerta.webhook.dispatcher.redis.dto.Dto;

public class RedisInfoDtoV1 extends Dto {

    public List<RedisGroupInfoDtoV1> redisStatus;

    private RedisInfoDtoV1() {
    }

    public static RedisInfoDtoV1 redisStatusDto() {
        return new RedisInfoDtoV1();
    }

    public RedisInfoDtoV1 withRedisStatus(List<RedisGroupInfoDtoV1> redisStatus) {
        this.redisStatus = redisStatus;
        return this;
    }
}
