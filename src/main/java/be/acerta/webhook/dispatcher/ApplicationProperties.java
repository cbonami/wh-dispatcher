package be.acerta.webhook.dispatcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@RefreshScope
@Component
public class ApplicationProperties {

    @Value("${redis.url}")
    private String redisUrl;
    @Value("${redis.password}")
    private String redisPassword;

    public String getRedisUrl() {
        return redisUrl;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

}