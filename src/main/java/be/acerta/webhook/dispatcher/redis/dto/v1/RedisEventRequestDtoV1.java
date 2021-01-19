package  be.acerta.webhook.dispatcher.redis.dto.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

@ToString
@EqualsAndHashCode
public class RedisEventRequestDtoV1 implements Serializable {
    
    @JsonProperty(required = true)
    @NotNull(message = "groupId mag niet leeg zijn")
    public String groupId;

    @JsonProperty(required = true)
    @NotNull(message = "bucketId mag niet leeg zijn")
    public String bucketId;

    @JsonProperty(required = true)
    @NotNull(message = "eventId mag niet leeg zijn")
    public String eventId;

    private RedisEventRequestDtoV1() {
    }

    public static RedisEventRequestDtoV1 redisEventRequestDtoV1() {
        return new RedisEventRequestDtoV1();
    }

    public RedisEventRequestDtoV1 withGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public RedisEventRequestDtoV1 withBucketId(String bucketId) {
        this.bucketId = bucketId;
        return this;
    }

    public RedisEventRequestDtoV1 withEventId(String eventId) {
        this.eventId = eventId;
        return this;
    }
}
