package be.acerta.webhook.dispatcher.redis.webhook;

import java.util.List;

import javax.inject.Inject;

import be.acerta.webhook.dispatcher.redis.EventStrategy;
import be.acerta.webhook.dispatcher.redis.webhook.WebhookRedisMessageProducer;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//import static org.springframework.context.annotation.FilterType.ANNOTATION;

@Configuration
// @ComponentScan(
// value = WebhookAdapterConfig.SKEDIFY_ADAPTER_BASE_PACKAGE,
// excludeFilters = @ComponentScan.Filter(type = ANNOTATION, value =
// Configuration.class))
public class WebhookAdapterConfig {

    // static final String SKEDIFY_ADAPTER_BASE_PACKAGE = BASE_PACKAGE +
    // ".skedify.adapter";

    @Inject
    private List<EventStrategy> eventStrategies;

    // @Inject
    // private ParameterService parameterService;

    @Bean
    public WebhookRedisClient webhookRedisClient(RedissonClient client) {
        return new WebhookRedisClient(client);
    }

    @Bean
    public WebhookRedisMessageListener webhookRedisMessageListener(WebhookRedisClient skedifyRedisClient) {
        return new WebhookRedisMessageListener(skedifyRedisClient, eventStrategies);
    }

    @Bean
    public WebhookRedisMessageProducer webhookRedisMessageProducer(WebhookRedisClient client) {
        return new WebhookRedisMessageProducer(client);
    }

}