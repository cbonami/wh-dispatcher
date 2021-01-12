package be.acerta.webhook.dispatcher.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.util.List;

@RedisHash("Application")
@Data
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

    protected Application() {
    }

    public Application(String url) {
        super();
        this.url = url;
        this.online = true;
    }

}
