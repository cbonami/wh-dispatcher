package be.acerta.webhook.dispatcher.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A mini-queue for e.g. a specific person. All messages in the bucket will be
 * delivered in fixed order.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bucket {
    private String id;
}
