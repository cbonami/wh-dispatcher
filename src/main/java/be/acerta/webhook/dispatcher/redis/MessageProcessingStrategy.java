package be.acerta.webhook.dispatcher.redis;

public interface MessageProcessingStrategy {

    boolean canProcess(MessageType eventType);

    void processMessage(String messageBody);

    MessageType getProcessedMessageType();
    
}
