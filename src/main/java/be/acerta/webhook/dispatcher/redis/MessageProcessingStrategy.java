package be.acerta.webhook.dispatcher.redis;

public interface MessageProcessingStrategy {

    boolean canHandle(MessageType eventType);

    void handleEventMessageBody(String messageBody, String tracingCorrelationId);

    MessageType getEventType();
}
