package be.acerta.webhook.dispatcher.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;

@RedisHash("Application")
@Data
@AllArgsConstructor
@Builder
public class Application {

    private Long id;

    private String url;

    private String name;

    @JsonIgnore
    private List<Message> messages;

    private Boolean online;

    protected Application() {
    }

    public Application(String url) {
        super();
        this.url = url;
        this.online = true;
    }

}
