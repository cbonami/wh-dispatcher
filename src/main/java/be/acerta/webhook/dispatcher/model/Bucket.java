package be.acerta.webhook.dispatcher.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

/**
 * A mini-queue for e.g. a specific person. All messages in the bucket will be
 * delivered in fixed order.
 */
@Data
@JsonInclude(Include.NON_NULL)
public class Bucket extends RepresentationModel<Bucket> {
    private String id;
}
