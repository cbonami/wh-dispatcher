package be.acerta.webhook.dispatcher.redis.maintenance;

import static be.acerta.webhook.dispatcher.redis.dto.v1.RedisGroupInfoDtoV1.redisGroupInfoDto;
import static be.acerta.webhook.dispatcher.redis.dto.v1.RedisInfoDtoV1.redisStatusDto;
import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import be.acerta.webhook.dispatcher.redis.RedisClient;
import be.acerta.webhook.dispatcher.redis.dto.v1.RedisGroupInfoDtoV1;
import be.acerta.webhook.dispatcher.redis.dto.v1.RedisInfoDtoV1;

@Named
public class RedisMaintenanceServiceImpl implements RedisMaintenanceService {

    @Inject
    private List<RedisClient> redisClients;

    @Override
    public void clear(BatchType batchType) {
        redisClients.forEach(RedisClient::cleanRedis);
    }

    @Override
    public void triggerProcessing(String groupId, String bucketId) {
        getRedisClientVoorGroupId(groupId).triggerProcessing(bucketId);
    }

    @Override
    public void forceProcessing(String groupId, String bucketId) {
        getRedisClientVoorGroupId(groupId).forceProcessing(bucketId);
    }

    @Override
    public void removeEvent(String groupId, String bucketId, Long eventId) {
        getRedisClientVoorGroupId(groupId).removeEvent(bucketId, eventId);
    }

    @Override
    public void removeBucket(String groupId, String bucketId) {
        getRedisClientVoorGroupId(groupId).removeBucket(bucketId);
    }

    @Override
    public boolean isProcessorLocked(String groupId, String bucketId) {
        return getRedisClientVoorGroupId(groupId).isProcessorLocked(bucketId);
    }

    private RedisClient getRedisClientVoorGroupId(String groupId) {
        return redisClients.stream().filter(client -> client.groupId().equals(groupId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No Redis client found for group id %s", groupId)));
    }

    @Override
    public RedisInfoDtoV1 getRedisInfo() {
        return redisStatusDto().withRedisStatus(redisClients.stream().map(this::getRedisGroupInfo).collect(toList()));
    }

    private RedisGroupInfoDtoV1 getRedisGroupInfo(RedisClient redisClient) {
        return redisGroupInfoDto().withId(redisClient.groupId())
                .withAantalBuckets(redisClient.getBuckets().keySet().size())
                .withBucketIds(redisClient.getBuckets().keySet())
                .withAantalWachtendeBuckets(redisClient.getAwaitRetries().keySet().size())
                .withWachtendeBuckets(redisClient.getAwaitRetries().keySet());
    }
}
