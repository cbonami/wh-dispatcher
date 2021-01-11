package be.acerta.webhook.dispatcher.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.sql.Timestamp;

@RedisHash("Message")
@Data
public class Message implements Serializable {

    static final long MESSAGE_TIMEOUT = 24 * 60 * 60 * 1000;

    @Id
    private String id;

    private String messageBody;

    private String contentType;

    private Timestamp timestamp;

    @Reference
    private Application application;

    protected Message() {
    }

    public Message(String messageBody, String contentType, Application application) {
        super();
        this.messageBody = messageBody;
        this.contentType = contentType;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.application = application;
    }


    public String getDestinationId() {
        return application.getId();
    }

    public String getDestinationUrl() {
        return application.getUrl();
    }

    public Boolean isMessageTimeout() {
        return timestamp.getTime() < System.currentTimeMillis() - MESSAGE_TIMEOUT;
    }


    @Override
    public String toString() {
        return String.format("Message[id=%d, messageBody='%s', contentType='%s']", id, messageBody, contentType);
    }
}
