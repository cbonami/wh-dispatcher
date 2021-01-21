package be.acerta.webhook.dispatcher.redis.webhook;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookMessageDto implements Serializable {

    private String id;
    private String data;
    private String eventType;
    private String webhookUrl;
    private String mimeType;

    // makes the message unique, so that the reciver can know if it has received the
    // message before when it is being resubmitted
    private String idempotencyKey;

}
