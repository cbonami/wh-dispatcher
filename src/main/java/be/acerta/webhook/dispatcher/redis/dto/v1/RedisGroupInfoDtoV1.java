package  be.acerta.webhook.dispatcher.redis.dto.v1;

import java.io.Serializable;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class RedisGroupInfoDtoV1 implements Serializable {

    public String id;
    public Integer aantalBuckets;
    public Set<String> bucketIds;
    public Integer aantalWachtendeBuckets;
    public Set<String> wachtendeBuckets;

    private RedisGroupInfoDtoV1() {
    }

    public static RedisGroupInfoDtoV1 redisGroupInfoDto() {
        return new RedisGroupInfoDtoV1();
    }

    public RedisGroupInfoDtoV1 withId(String id) {
        this.id = id;
        return this;
    }

    public RedisGroupInfoDtoV1 withAantalBuckets(Integer aantalBuckets) {
        this.aantalBuckets = aantalBuckets;
        return this;
    }

    public RedisGroupInfoDtoV1 withBucketIds(Set<String> bucketIds) {
        this.bucketIds = bucketIds;
        return this;
    }

    public RedisGroupInfoDtoV1 withAantalWachtendeBuckets(Integer aantalWachtendeBuckets) {
        this.aantalWachtendeBuckets = aantalWachtendeBuckets;
        return this;
    }

    public RedisGroupInfoDtoV1 withWachtendeBuckets(Set<String> wachtendeBuckets) {
        this.wachtendeBuckets = wachtendeBuckets;
        return this;
    }
}
