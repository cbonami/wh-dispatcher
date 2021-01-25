package be.acerta.webhook.dispatcher.redis.webhook;

import be.acerta.webhook.dispatcher.JsonUtil;
import be.acerta.webhook.dispatcher.model.Message;
import be.acerta.webhook.dispatcher.redis.RedisClient;
import be.acerta.webhook.dispatcher.redis.RedisMessageProducer;

public class WebhookRedisMessageProducer extends RedisMessageProducer {

    public WebhookRedisMessageProducer(RedisClient client) {
        super(client);
    }

    public Message publish(String appName, String bucketId, WebhookMessageDto dto) {

        super.doPublish(appName + "/" + bucketId, JsonUtil.objectToJson(dto));

        return Message.builder().id(dto.getId()).idempotencyKey(dto.getIdempotencyKey()).build();
    }

}
