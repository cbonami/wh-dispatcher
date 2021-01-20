package be.acerta.webhook.dispatcher.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.hateoas.RepresentationModel;

/**
 * An subscribing application corresponds to an endpoint (url) that we will POST
 * messages to.
 */
@RedisHash("Application")
@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class Application extends RepresentationModel<Application> {

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
