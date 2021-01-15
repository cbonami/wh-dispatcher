package be.acerta.webhook.dispatcher.redis;

import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Named
public class RedisEventHandler {

    private static final Logger LOGGER = getLogger(RedisEventHandler.class);

    @Inject
    private List<RedisMessageListener> redisMessageListeners;

    @EventListener(classes = ApplicationReadyEvent.class)
    @Order
    public void onApplicationReady() {
        LOGGER.debug("--- Opstarten Redis message listeners: Application Ready ---");
        redisMessageListeners.forEach(RedisMessageListener::startListeners);
    }
}
