package be.acerta.webhook.dispatcher.redis.maintenance.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class RedisInfoDto implements Serializable {

    public List<RedisGroupInfoDto> redisStatus;

    public static RedisInfoDto redisStatusDto() {
        return new RedisInfoDto();
    }

    public RedisInfoDto withRedisStatus(List<RedisGroupInfoDto> redisStatus) {
        this.redisStatus = redisStatus;
        return this;
    }
}
