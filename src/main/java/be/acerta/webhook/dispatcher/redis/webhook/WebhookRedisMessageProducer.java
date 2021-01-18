package be.acerta.webhook.dispatcher.redis.webhook;

import static be.acerta.webhook.dispatcher.JsonUtil.jsonToObject;

import be.acerta.webhook.dispatcher.redis.RedisMessageProducer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebhookRedisMessageProducer extends RedisMessageProducer {

    public WebhookRedisMessageProducer(WebhookRedisClient client) {
        super(client);
    }

    public void doPublish(String message, String eventName, String correlationId) {
        WebhookEventDto skedifyEventDto = jsonToObject(message, WebhookEventDto.class);
        log.info("Publishing event {} met event id {} naar bucket id {}", eventName, skedifyEventDto.id,
                skedifyEventDto.bucketId);
        publish(skedifyEventDto.bucketId, message);
    }
}
