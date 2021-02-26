package be.acerta.webhook.dispatcher.redis;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

@Named
public class RedisEventHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RedisEventHandler.class);
    @Inject
    private List<RedisMessageListener> redisMessageListeners;

    @EventListener(classes = ApplicationReadyEvent.class)
    @Order
    public void onApplicationReady() {
        log.info("--- Starting Redis message listeners: Application Ready ---");
        redisMessageListeners.forEach(RedisMessageListener::startListeners);
    }
}
