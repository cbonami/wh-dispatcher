package be.acerta.webhook.dispatcher.restcontrollers.dto;

import java.io.Serializable;
import java.util.Set;

public class RedisGroupInfoDto implements Serializable {
    private static final long serialVersionUID = -729113616264985503L;
    //private String id;
    private Integer nbBuckets;
    private Set<String> bucketIds;
    private Integer nbWaitingBuckets;
    private Set<String> waitingBuckets;

    public static RedisGroupInfoDto redisGroupInfoDto() {
        return new RedisGroupInfoDto();
    }

/*     public RedisGroupInfoDto withId(String id) {
        this.id = id;
        return this;
    } */
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

    public RedisGroupInfoDto() {
    }

    public Integer getNbBuckets() {
        return this.nbBuckets;
    }

    public Set<String> getBucketIds() {
        return this.bucketIds;
    }

    public Integer getNbWaitingBuckets() {
        return this.nbWaitingBuckets;
    }

    public Set<String> getWaitingBuckets() {
        return this.waitingBuckets;
    }

    public void setNbBuckets(final Integer nbBuckets) {
        this.nbBuckets = nbBuckets;
    }

    public void setBucketIds(final Set<String> bucketIds) {
        this.bucketIds = bucketIds;
    }

    public void setNbWaitingBuckets(final Integer nbWaitingBuckets) {
        this.nbWaitingBuckets = nbWaitingBuckets;
    }

    public void setWaitingBuckets(final Set<String> waitingBuckets) {
        this.waitingBuckets = waitingBuckets;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof RedisGroupInfoDto)) return false;
        final RedisGroupInfoDto other = (RedisGroupInfoDto) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$nbBuckets = this.getNbBuckets();
        final Object other$nbBuckets = other.getNbBuckets();
        if (this$nbBuckets == null ? other$nbBuckets != null : !this$nbBuckets.equals(other$nbBuckets)) return false;
        final Object this$nbWaitingBuckets = this.getNbWaitingBuckets();
        final Object other$nbWaitingBuckets = other.getNbWaitingBuckets();
        if (this$nbWaitingBuckets == null ? other$nbWaitingBuckets != null : !this$nbWaitingBuckets.equals(other$nbWaitingBuckets)) return false;
        final Object this$bucketIds = this.getBucketIds();
        final Object other$bucketIds = other.getBucketIds();
        if (this$bucketIds == null ? other$bucketIds != null : !this$bucketIds.equals(other$bucketIds)) return false;
        final Object this$waitingBuckets = this.getWaitingBuckets();
        final Object other$waitingBuckets = other.getWaitingBuckets();
        if (this$waitingBuckets == null ? other$waitingBuckets != null : !this$waitingBuckets.equals(other$waitingBuckets)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof RedisGroupInfoDto;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $nbBuckets = this.getNbBuckets();
        result = result * PRIME + ($nbBuckets == null ? 43 : $nbBuckets.hashCode());
        final Object $nbWaitingBuckets = this.getNbWaitingBuckets();
        result = result * PRIME + ($nbWaitingBuckets == null ? 43 : $nbWaitingBuckets.hashCode());
        final Object $bucketIds = this.getBucketIds();
        result = result * PRIME + ($bucketIds == null ? 43 : $bucketIds.hashCode());
        final Object $waitingBuckets = this.getWaitingBuckets();
        result = result * PRIME + ($waitingBuckets == null ? 43 : $waitingBuckets.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "RedisGroupInfoDto(nbBuckets=" + this.getNbBuckets() + ", bucketIds=" + this.getBucketIds() + ", nbWaitingBuckets=" + this.getNbWaitingBuckets() + ", waitingBuckets=" + this.getWaitingBuckets() + ")";
    }
}
