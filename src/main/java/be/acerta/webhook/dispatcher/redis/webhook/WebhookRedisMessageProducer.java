package be.acerta.webhook.dispatcher.redis.webhook;

import java.util.UUID;

import be.acerta.webhook.dispatcher.JsonUtil;
import be.acerta.webhook.dispatcher.redis.RedisClient;
import be.acerta.webhook.dispatcher.redis.RedisMessageProducer;
import org.springframework.util.MimeType;

public class WebhookRedisMessageProducer extends RedisMessageProducer {

    public WebhookRedisMessageProducer(RedisClient client) {
        super(client);
    }

    public void publish(String appName, String webhookUrl, String queueId, String hmac, String messageType,
            String message, MimeType mediaType) {

        // put json data in an envelope
        // todo apply hmac encryption
        WebhookEventDto webhookEventDto = new WebhookEventDto(UUID.randomUUID().toString(), message, messageType,
                webhookUrl, UUID.randomUUID().toString(), mediaType.getType());

        super.doPublish(appName + "/" + queueId, JsonUtil.objectToJson(webhookEventDto));
    }

}
