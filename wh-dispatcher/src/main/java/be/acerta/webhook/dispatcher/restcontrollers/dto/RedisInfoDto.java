package be.acerta.webhook.dispatcher.restcontrollers.dto;

import java.io.Serializable;
import java.util.List;
// @fixme merge RedisGroupInfoDto; there's only 1 group
public class RedisInfoDto implements Serializable {
    private List<RedisGroupInfoDto> redisStatus;

    public static RedisInfoDto redisStatusDto() {
        return new RedisInfoDto();
    }

    public RedisInfoDto withRedisStatus(List<RedisGroupInfoDto> redisStatus) {
        this.redisStatus = redisStatus;
        return this;
    }

    public RedisInfoDto() {
    }

    public List<RedisGroupInfoDto> getRedisStatus() {
        return this.redisStatus;
    }

    public void setRedisStatus(final List<RedisGroupInfoDto> redisStatus) {
        this.redisStatus = redisStatus;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof RedisInfoDto)) return false;
        final RedisInfoDto other = (RedisInfoDto) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$redisStatus = this.getRedisStatus();
        final Object other$redisStatus = other.getRedisStatus();
        if (this$redisStatus == null ? other$redisStatus != null : !this$redisStatus.equals(other$redisStatus)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof RedisInfoDto;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $redisStatus = this.getRedisStatus();
        result = result * PRIME + ($redisStatus == null ? 43 : $redisStatus.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "RedisInfoDto(redisStatus=" + this.getRedisStatus() + ")";
    }
}
