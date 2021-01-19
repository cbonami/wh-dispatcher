package be.acerta.webhook.dispatcher.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

/**
 * An subscribing application corresponds to an endpoint (url) that we will POST
 * messages to. 
 */
@RedisHash("Application")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

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
