package be.acerta.webhook.dispatcher.redis.dto.v1;

import java.io.Serializable;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class RedisInfoDtoV1 implements Serializable {

    private static final long serialVersionUID = 8050390751417155279L;

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
