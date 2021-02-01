package be.acerta.webhook.dispatcher.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

/**
 * A mini-queue for e.g. a specific person. All messages in the bucket will be
 * delivered in fixed order.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Bucket extends RepresentationModel<Bucket> {
    private String id;
    private List<Message> messages;
}
