package be.acerta.webhook.dispatcher.redis.webhook;

import static be.acerta.webhook.dispatcher.JsonUtil.jsonToObject;

import java.util.Collections;

import javax.inject.Inject;

import be.acerta.webhook.dispatcher.redis.EventStrategy;
import be.acerta.webhook.dispatcher.redis.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class WebhookEventStrategy implements EventStrategy {

    @Inject
    private RestTemplate restTemplate;

    @Override
    public boolean canHandle(EventType eventType) {
        return eventType.equals(getEventType());
    }

    @Override
    @Transactional
    public void handleEventMessageBody(String messageBody, String tracingCorrelationId) {
        log.debug("handleEventMessageBody - {} \n", tracingCorrelationId, messageBody);
        WebhookEventDto webhookEventDto = jsonToObject(messageBody, WebhookEventDto.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        // todo pass tracingCorrelationId
        HttpEntity<String> entity = new HttpEntity<>("body", headers);
        restTemplate.exchange(webhookEventDto.getWebhookUrl(), HttpMethod.POST, entity, String.class);
    }

    public EventType getEventType() {
        return EventType.WEBHOOK;
    }

}
