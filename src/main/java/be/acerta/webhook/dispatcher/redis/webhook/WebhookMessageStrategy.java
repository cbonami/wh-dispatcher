package be.acerta.webhook.dispatcher.redis.webhook;

import static be.acerta.webhook.dispatcher.redis.JsonUtil.jsonToObject;

import java.util.Collections;

import javax.inject.Inject;

import be.acerta.webhook.dispatcher.redis.MessageProcessingStrategy;
import be.acerta.webhook.dispatcher.redis.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class WebhookMessageStrategy implements MessageProcessingStrategy {

    @Inject
    private RestTemplate restTemplate;

    @Override
    public boolean canProcess(MessageType eventType) {
        return eventType.equals(getProcessedMessageType());
    }

    @Override
    // @Transactional
    public void processMessage(String message) {
        log.debug("processMessage - {} \n", message);
        WebhookMessageDto webhookEventDto = jsonToObject(message, WebhookMessageDto.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        // todo pass tracingCorrelationId as header
        HttpEntity<String> entity = new HttpEntity<>(webhookEventDto.getData(), headers);
        ResponseEntity<String> response = restTemplate.exchange(webhookEventDto.getWebhookUrl(), HttpMethod.POST,
                entity, String.class);
        log.debug("Response status = {}", response.getStatusCode());
    }

    public MessageType getProcessedMessageType() {
        return MessageType.WEBHOOK_V1;
    }

}
