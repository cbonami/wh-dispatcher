package be.acerta.webhook.dispatcher.redis;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

@Named
@Slf4j
public class RedisEventHandler {

    @Inject
    private List<RedisMessageListener> redisMessageListeners;

    @EventListener(classes = ApplicationReadyEvent.class)
    @Order
    public void onApplicationReady() {
        log.info("--- Opstarten Redis message listeners: Application Ready ---");
        redisMessageListeners.forEach(RedisMessageListener::startListeners);
    }
}
