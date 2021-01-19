package be.acerta.webhook.dispatcher.redis;

public interface EventStrategy {

    boolean canHandle(EventType eventType);

    void handleEventMessageBody(String messageBody, String tracingCorrelationId);

    EventType getEventType();
}
