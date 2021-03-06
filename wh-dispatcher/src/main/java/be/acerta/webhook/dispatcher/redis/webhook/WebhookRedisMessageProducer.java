package be.acerta.webhook.dispatcher.redis.webhook;

import java.util.Collection;

import be.acerta.webhook.dispatcher.model.Message;
import be.acerta.webhook.dispatcher.redis.JsonUtil;
import be.acerta.webhook.dispatcher.redis.RedisClient;
import be.acerta.webhook.dispatcher.redis.RedisMessageProducer;
import com.google.common.collect.Lists;

public class WebhookRedisMessageProducer extends RedisMessageProducer {

    public WebhookRedisMessageProducer(RedisClient client) {
        super(client);
    }

    /**
     * Schedules the sending of a message that belongs to a logical bucket. The
     * message is sent to a specific endpoint (URL) as identified by the webhookId.
     * Message is first stored in REDIS, where it will be picked up by a listener.
     * 
     * @param webhookId  identifies the webhook
     * @param whBucketId identifies the bucket WITHIN THE WEBHOOK
     * @param dto
     * @return the message as it will be posted later on
     * @see be.acerta.webhook.dispatcher.redis.webhook.RedisMessageListener
     */
    public Message asyncSend(String webhookId, String whBucketId, WebhookMessageDto dto) {

        super.doAsyncSend(webhookId + "|" + whBucketId, JsonUtil.objectToJson(dto));

        return Message.builder().id(dto.getId()).idempotencyKey(dto.getIdempotencyKey()).delivery(dto.getDelivery())
                .type(dto.getType()).build();
    }

    /**
     * Publishes a message PubSub-style. The system inspects the message, and
     * figures out which webhooks are interested in the message's type. Then it
     * ```asyncSend```-s the message to the subscribed webhook(s).
     * 
     * @param whBucketId the non-absolute i.e. logical webhookId
     */
    public Collection<Message> publish(String whBucketId, WebhookMessageDto dto) {

        dto.getDelivery();

        // @fixme send message to all webhooks that are interested (subscribed) to this
        // type of message ('fan out')

        return Lists.<Message>newArrayList();
    }

}
