package be.acerta.webhook.dispatcher.redis.webhook;

import java.util.List;
import java.util.Optional;

import be.acerta.webhook.dispatcher.redis.MessageProcessingStrategy;
import be.acerta.webhook.dispatcher.redis.MessageDeliveryType;
import be.acerta.webhook.dispatcher.redis.RedisMessageListener;
import org.json.JSONObject;


/**
 * Reads messages from REDIS and calls a webhook for each of them.
 */

public class WebhookRedisMessageListener extends RedisMessageListener {

    public WebhookRedisMessageListener(WebhookRedisClient client, List<MessageProcessingStrategy> messageProcessingStrategies) {
        super(client, messageProcessingStrategies);
    }

    @Override
    protected Optional<MessageDeliveryType> determineMessageType(String message) {
        return MessageDeliveryType.fromNaam(new JSONObject(message).getString("delivery"));
    }

    @Override
    protected Long getRedisMaxRetryInterval() {
        // @fixme replace constant by application property
        return 10000L;
    }
}
