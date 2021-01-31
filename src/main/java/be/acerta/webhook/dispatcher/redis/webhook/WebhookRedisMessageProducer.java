package be.acerta.webhook.dispatcher.redis.webhook;

import be.acerta.webhook.dispatcher.model.Message;
import be.acerta.webhook.dispatcher.redis.JsonUtil;
import be.acerta.webhook.dispatcher.redis.RedisClient;
import be.acerta.webhook.dispatcher.redis.RedisMessageProducer;

public class WebhookRedisMessageProducer extends RedisMessageProducer {

    public WebhookRedisMessageProducer(RedisClient client) {
        super(client);
    }

    public Message publish(String appId, String bucketId, WebhookMessageDto dto) {

        super.doPublish(appId + "/" + bucketId, JsonUtil.objectToJson(dto));

        return Message.builder().id(dto.getId()).idempotencyKey(dto.getIdempotencyKey()).build();
    }

}
