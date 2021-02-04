package be.acerta.webhook.dispatcher.redis.webhook;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookMessageDto implements Serializable {

    private String id;
    private String data;

    /** how (protocol + agreed post-api) message needs to be delivered, i.e. webhook_v1 */
    private String delivery;
    
    /** class of the message i.e. SomethingHappenedEvent */
    private String type;

    private String webhookUrl;
    private String mediaType;

    // makes the message unique, so that the reciver can know if it has received the
    // message before when it is being resubmitted
    private String idempotencyKey;

}
