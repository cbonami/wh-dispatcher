package be.acerta.webhook.dispatcher.redis;

public interface EventStrategy {

    boolean canHandle(EventType eventType);

    void handleEventMessageBody(String messageBody, String correlationId, RedisClient redisClient);

    EventType getEventType();
}
