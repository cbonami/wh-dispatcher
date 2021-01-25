package be.acerta.webhook.dispatcher.redis.webhook;

import java.util.List;
import java.util.Optional;

import be.acerta.webhook.dispatcher.redis.MessageProcessingStrategy;
import be.acerta.webhook.dispatcher.redis.MessageType;
import be.acerta.webhook.dispatcher.redis.RedisMessageListener;
import org.json.JSONObject;

//bonami import static ParameterNaam.SKEDIFY_REDIS_MAX_RETRY_INTERVAL;

/**
 * Reads messages from REDIS and calls a webhook for each of them.
 */

public class WebhookRedisMessageListener extends RedisMessageListener {

    public WebhookRedisMessageListener(WebhookRedisClient client, List<MessageProcessingStrategy> eventStrategies) {
        super(client, eventStrategies);
    }

    @Override
    protected Optional<MessageType> determineMessageType(String message) {
        return MessageType.fromNaam(new JSONObject(message).getString("type"));
    }

    @Override
    protected Long getRedisMaxRetryInterval() {
        // bonami return
        // parameterService.findOneExistingByNaam(SKEDIFY_REDIS_MAX_RETRY_INTERVAL).getWaarde();
        return 10000L;
    }
}
