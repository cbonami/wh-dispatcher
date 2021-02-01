package be.acerta.webhook.dispatcher.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Data
@JsonInclude(Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message extends RepresentationModel<Message> {
    private String id;
    private String idempotencyKey;
    private String data;
    private String type;
    private String webhookUrl;
    private String mimeType;
}
