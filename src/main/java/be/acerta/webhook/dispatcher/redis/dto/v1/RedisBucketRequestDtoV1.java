package  be.acerta.webhook.dispatcher.redis.dto.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode
@ToString
public class RedisBucketRequestDtoV1 implements Serializable {

    private static final long serialVersionUID = 5876705350569321863L;

    @JsonProperty(required = true)
    @NotNull(message = "groupId mag niet leeg zijn")
    public String groupId;

    @JsonProperty(required = true)
    @NotNull(message = "bucketId mag niet leeg zijn")
    public String bucketId;

    private RedisBucketRequestDtoV1() {
    }

    public static RedisBucketRequestDtoV1 redisBucketRequestDtoV1() {
        return new RedisBucketRequestDtoV1();
    }

    public RedisBucketRequestDtoV1 withGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public RedisBucketRequestDtoV1 withBucketId(String bucketId) {
        this.bucketId = bucketId;
        return this;
    }
}
