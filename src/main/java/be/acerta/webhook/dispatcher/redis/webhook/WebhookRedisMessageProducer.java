package be.acerta.webhook.dispatcher.redis.webhook;

import java.util.UUID;

import be.acerta.webhook.dispatcher.JsonUtil;
import be.acerta.webhook.dispatcher.redis.RedisClient;
import be.acerta.webhook.dispatcher.redis.RedisMessageProducer;

public class WebhookRedisMessageProducer extends RedisMessageProducer {

    public WebhookRedisMessageProducer(RedisClient client) {
        super(client);
    }

    public void publish(String appName, String webhookUrl, String queueId, String hmac, String messageType,
            String message) {

        // put json data in an envelope
        // todo apply hmac encryption
        WebhookEventDto webhookEventDto = new WebhookEventDto(message, messageType, webhookUrl, UUID.randomUUID().toString());

        super.doPublish(appName + "/" + queueId, JsonUtil.objectToJson(webhookEventDto));
    }

}
