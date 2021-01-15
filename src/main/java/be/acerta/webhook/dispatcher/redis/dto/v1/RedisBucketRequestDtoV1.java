package  be.acerta.webhook.dispatcher.redis.dto.v1;

import com.fasterxml.jackson.annotation.JsonProperty;

import be.acerta.webhook.dispatcher.redis.dto.Dto;

import javax.validation.constraints.NotNull;

public class RedisBucketRequestDtoV1 extends Dto {
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
