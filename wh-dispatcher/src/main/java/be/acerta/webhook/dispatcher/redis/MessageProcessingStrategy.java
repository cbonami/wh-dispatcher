package be.acerta.webhook.dispatcher.redis;

public interface MessageProcessingStrategy {

    boolean canProcess(MessageDeliveryType eventType);

    void processMessage(String messageBody);

    MessageDeliveryType getProcessedMessageType();
    
}
