package be.acerta.webhook.dispatcher.redis.webhook;

import java.util.List;

import be.acerta.webhook.dispatcher.redis.MessageProcessingStrategy;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebhookAdapterConfig {

    @Bean
    public WebhookRedisClient webhookRedisClient(RedissonClient client) {
        return new WebhookRedisClient(client);
    }

    @Bean
    public WebhookRedisMessageListener webhookRedisMessageListener(WebhookRedisClient webhookRedisClient,
            List<MessageProcessingStrategy> messageProcessingStrategies) {
        return new WebhookRedisMessageListener(webhookRedisClient, messageProcessingStrategies);
    }

    @Bean
    public WebhookRedisMessageProducer webhookRedisMessageProducer(WebhookRedisClient client) {
        return new WebhookRedisMessageProducer(client);
    }

}