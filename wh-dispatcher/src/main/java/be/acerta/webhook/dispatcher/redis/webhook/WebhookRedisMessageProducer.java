package be.acerta.webhook.dispatcher.redis.webhook;

import be.acerta.webhook.dispatcher.model.Message;
import be.acerta.webhook.dispatcher.redis.JsonUtil;
import be.acerta.webhook.dispatcher.redis.RedisClient;
import be.acerta.webhook.dispatcher.redis.RedisMessageProducer;

public class WebhookRedisMessageProducer extends RedisMessageProducer {

    public WebhookRedisMessageProducer(RedisClient client) {
        super(client);
    }

    /**
     * Schedules the sending of a message that belongs to a logical bucket. The
     * message is sent to a specific endpoint (URL) as identified by the webhookId.
     * Message is first stored in REDIS, where it will be picked up by a listener.
     * 
     * @param webhookId   identifies the webhook
     * @param appBucketId identifies the bucket WITHIN THE WEBHOOK
     * @param dto
     * @return the id and idempotency key that have been generated for this message
     * @see be.acerta.webhook.dispatcher.redis.webhook.RedisMessageListener
     */
    public Message publish(String webhookId, String appBucketId, WebhookMessageDto dto) {

        super.doPublish(webhookId + "|" + appBucketId, JsonUtil.objectToJson(dto));

        return Message.builder().id(dto.getId()).idempotencyKey(dto.getIdempotencyKey()).build();
    }

}
