package be.acerta.webhook.dispatcher.services;

public class MessageProcessorException extends Exception {

    private static final long serialVersionUID = 1L;

    public MessageProcessorException(String message) {
        super(message);
    }

}
