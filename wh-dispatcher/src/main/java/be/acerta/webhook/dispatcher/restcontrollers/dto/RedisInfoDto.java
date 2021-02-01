package be.acerta.webhook.dispatcher.restcontrollers.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

// @fixme merge RedisGroupInfoDto; there's only 1 group
@Data
public class RedisInfoDto implements Serializable {

    private List<RedisGroupInfoDto> redisStatus;

    public static RedisInfoDto redisStatusDto() {
        return new RedisInfoDto();
    }

    public RedisInfoDto withRedisStatus(List<RedisGroupInfoDto> redisStatus) {
        this.redisStatus = redisStatus;
        return this;
    }
}
