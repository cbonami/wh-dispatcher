package be.acerta.webhook.dispatcher.redis.webhook;

import be.acerta.webhook.dispatcher.redis.EventType;

public class WebhookEventMetaData {

    private EventType eventType;
    private Long skedifyEventId;
    // private IdempotencyKey idempotencyKey;

    public WebhookEventMetaData(EventType eventType, Long skedifyEventId/* , IdempotencyKey idempotencyKey */) {
        this.eventType = eventType;
        this.skedifyEventId = skedifyEventId;
        // this.idempotencyKey = idempotencyKey;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Long getSkedifyEventId() {
        return skedifyEventId;
    }

    /*
     * public IdempotencyKey getIdempotencyKey() { return idempotencyKey; }
     */
}
