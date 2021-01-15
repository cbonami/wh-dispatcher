package be.acerta.webhook.dispatcher.events;

import be.acerta.webhook.dispatcher.model.Message;
import org.springframework.context.ApplicationEvent;

public class MessageReceivedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private Message message;

    public MessageReceivedEvent(Object source, Message message) {
        super(source);
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

}
