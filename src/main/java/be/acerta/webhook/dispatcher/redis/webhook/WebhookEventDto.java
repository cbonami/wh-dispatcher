package be.acerta.webhook.dispatcher.redis.webhook;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookEventDto implements Serializable {

    //private Long id;
    //private String queueId;
    private String jsonData;
    private String eventType;
    private String webhookUrl;
    private String mimeType;

    // makes the message unique, so that the reciver can know if it has received the message before when it is being resubmitted
    private String idempotencyKey;

}
