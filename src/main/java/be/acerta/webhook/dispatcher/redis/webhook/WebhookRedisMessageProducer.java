package be.acerta.webhook.dispatcher.redis.webhook;

import java.util.UUID;

import be.acerta.webhook.dispatcher.JsonUtil;
import be.acerta.webhook.dispatcher.model.Message;
import be.acerta.webhook.dispatcher.redis.RedisClient;
import be.acerta.webhook.dispatcher.redis.RedisMessageProducer;
import org.springframework.util.MimeType;

public class WebhookRedisMessageProducer extends RedisMessageProducer {

    public WebhookRedisMessageProducer(RedisClient client) {
        super(client);
    }

    public Message publish(String appName, String webhookUrl, String bucketId, String hmac, String messageType,
            String message, MimeType mediaType) {

        // put json data in an envelope
        // todo apply hmac encryption
        final String id = UUID.randomUUID().toString();
        final String idempotencyKey = UUID.randomUUID().toString();
        WebhookMessageDto webhookEventDto = new WebhookMessageDto(id, message, messageType,
                webhookUrl, idempotencyKey, mediaType.getType());

        super.doPublish(appName + "/" + bucketId, JsonUtil.objectToJson(webhookEventDto));

        return Message.builder().id(idempotencyKey).idempotencyKey(idempotencyKey).build();
    }

}
