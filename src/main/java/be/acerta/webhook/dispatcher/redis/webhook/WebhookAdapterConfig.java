package be.acerta.webhook.dispatcher.redis.webhook;

import java.util.List;

import be.acerta.webhook.dispatcher.redis.EventStrategy;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class WebhookAdapterConfig {

    // @Inject
    // private List<EventStrategy> eventStrategies;

    // @Inject
    // private ParameterService parameterService;

    @Bean
    public WebhookRedisClient webhookRedisClient(RedissonClient client) {
        return new WebhookRedisClient(client);
    }

    @Bean
    public WebhookRedisMessageListener webhookRedisMessageListener(WebhookRedisClient webhookRedisClient, List<EventStrategy> eventStrategies) {
        return new WebhookRedisMessageListener(webhookRedisClient, eventStrategies);
    }

    @Bean
    public WebhookRedisMessageProducer webhookRedisMessageProducer(WebhookRedisClient client) {
        return new WebhookRedisMessageProducer(client);
    }

}