package be.acerta.webhook.dispatcher.redis.webhook;

import static be.acerta.webhook.dispatcher.JsonUtil.jsonToObject;

import javax.inject.Named;

import be.acerta.webhook.dispatcher.redis.EventStrategy;
import be.acerta.webhook.dispatcher.redis.EventType;
import be.acerta.webhook.dispatcher.redis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class WebhookEventStrategy implements EventStrategy {

    /*
     * @Inject private IdempotencyService idempotencyService;
     */

    protected RedisClient redisClient;

    @Override
    public boolean canHandle(EventType eventType) {
        return eventType.equals(getEventType());
    }

    @Override
    @Transactional
    public void handleEventMessageBody(String messageBody, String correlationId, RedisClient redisClient) {
        this.redisClient = redisClient;
        WebhookEventDto webhookEventDto = jsonToObject(messageBody, WebhookEventDto.class);
        // IdempotencyKey idempotencyKey = createOrGetIdempotencyKey(getEventType(), WebhookEventDto.id);
        WebhookEventMetaData webhookEventMetaData = new WebhookEventMetaData(getEventType(), webhookEventDto.id);
        log.info(String.format("Handling Webhook event id %s met boeco event id %s", webhookEventDto.id, correlationId));
        //KibanaLogger.setVdabProcessId(correlationId);
        handle(webhookEventDto.jsonData, webhookEventMetaData);
    }

/*     private IdempotencyKey createOrGetIdempotencyKey(EventType eventType, Long WebhookEventId) {
        return idempotencyService.findOrCreate(WebhookEventId, eventType);
    }
 */
    public EventType getEventType() {
        return EventType.WEBHOOK;
    }

    protected void handle(String eventData, WebhookEventMetaData webhookEventMetaData) {
        // todo invoke webhook
        log.debug("handle event {}", eventData);
    }
}

