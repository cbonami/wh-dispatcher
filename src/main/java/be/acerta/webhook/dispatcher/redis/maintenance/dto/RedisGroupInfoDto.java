package be.acerta.webhook.dispatcher.redis.maintenance.dto;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;

@Data
public class RedisGroupInfoDto implements Serializable {

    private static final long serialVersionUID = -729113616264985503L;

    private String id;
    private Integer nbBuckets;
    private Set<String> bucketIds;
    private Integer nbWaitingBuckets;
    private Set<String> waitingBuckets;

    public static RedisGroupInfoDto redisGroupInfoDto() {
        return new RedisGroupInfoDto();
    }

    public RedisGroupInfoDto withId(String id) {
        this.id = id;
        return this;
    }

    public RedisGroupInfoDto withAantalBuckets(Integer aantalBuckets) {
        this.nbBuckets = aantalBuckets;
        return this;
    }

    public RedisGroupInfoDto withBucketIds(Set<String> bucketIds) {
        this.bucketIds = bucketIds;
        return this;
    }

    public RedisGroupInfoDto withAantalWachtendeBuckets(Integer aantalWachtendeBuckets) {
        this.nbWaitingBuckets = aantalWachtendeBuckets;
        return this;
    }

    public RedisGroupInfoDto withWachtendeBuckets(Set<String> wachtendeBuckets) {
        this.waitingBuckets = wachtendeBuckets;
        return this;
    }
}
