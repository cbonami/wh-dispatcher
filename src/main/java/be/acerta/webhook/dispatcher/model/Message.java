package be.acerta.webhook.dispatcher.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Data
@JsonInclude(Include.NON_NULL)
@Builder
public class Message extends RepresentationModel<Bucket> {
    private String id;
    private String idempotencyKey;
}
