package be.acerta.webhook.dispatcher.redis.specific;

import be.acerta.webhook.dispatcher.redis.EventType;
import be.acerta.webhook.dispatcher.redis.EventStrategy;
import be.acerta.webhook.dispatcher.redis.RedisMessageListener;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;

//bonami import static ParameterNaam.SKEDIFY_REDIS_MAX_RETRY_INTERVAL;

public class SkedifyRedisMessageListener extends RedisMessageListener {

    public SkedifyRedisMessageListener(SkedifyRedisClient client, List<EventStrategy> eventStrategies) {
        super(client, eventStrategies);
    }

    @Override
    protected Optional<EventType> determineEventType(String message) {
        return EventType.fromNaam(new JSONObject(message).getString("eventType"));
    }

    @Override
    protected String getCorrelationId(String message) {
        return String.valueOf(new JSONObject(new JSONObject(message).getString("jsonData")).getJSONObject("boecoEventId").getLong("value"));
    }

    @Override
    protected Long getRedisMaxRetryInterval() {
        //bonami return parameterService.findOneExistingByNaam(SKEDIFY_REDIS_MAX_RETRY_INTERVAL).getWaarde();
        return 10000L;
    }
}
