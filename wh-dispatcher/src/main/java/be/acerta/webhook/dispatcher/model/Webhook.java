package be.acerta.webhook.dispatcher.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.hateoas.RepresentationModel;

/**
 * Physically spoken, a ```Webhook``` represents an endpoint (url) that the dispatcher will POST
 * messages to. However, multiple Webhook-instances can share the same url. The latter
 * means that, from a logical point of view, a ```Webhook``` can also be seen as a
 * named point-to-point 'Queue' from a (set of systems) to a particular other system.
 */
@RedisHash("Webhook")
@Data
@JsonInclude(Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Webhook extends RepresentationModel<Webhook> {

    @Id
    private String id;

    private String url;

    @Indexed
    private String name;

    @Indexed
    private Boolean online;

    public void setOffline(Boolean offline) {
        this.online = false;
    }

}
