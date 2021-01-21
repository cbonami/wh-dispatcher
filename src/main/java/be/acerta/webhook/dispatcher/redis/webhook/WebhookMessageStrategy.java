package be.acerta.webhook.dispatcher.redis.webhook;

import static be.acerta.webhook.dispatcher.JsonUtil.jsonToObject;

import java.util.Collections;

import javax.inject.Inject;

import be.acerta.webhook.dispatcher.redis.MessageProcessingStrategy;
import be.acerta.webhook.dispatcher.redis.MessageType;
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
public class WebhookMessageStrategy implements MessageProcessingStrategy {

    @Inject
    private RestTemplate restTemplate;

    @Override
    public boolean canHandle(MessageType eventType) {
        return eventType.equals(getEventType());
    }

    @Override
    @Transactional
    public void handleEventMessageBody(String messageBody, String tracingCorrelationId) {
        log.debug("handleEventMessageBody - {} \n", tracingCorrelationId, messageBody);
        WebhookMessageDto webhookEventDto = jsonToObject(messageBody, WebhookMessageDto.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        // todo pass tracingCorrelationId
        HttpEntity<String> entity = new HttpEntity<>(webhookEventDto.getData(), headers);
        restTemplate.exchange(webhookEventDto.getWebhookUrl(), HttpMethod.POST, entity, String.class);
    }

    public MessageType getEventType() {
        return MessageType.WEBHOOK;
    }

}
