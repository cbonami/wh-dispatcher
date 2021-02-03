package be.acerta.webhook.dispatcher.redis.webhook;

import static be.acerta.webhook.dispatcher.redis.JsonUtil.jsonToObject;

import java.util.Collections;

import javax.inject.Inject;

import be.acerta.webhook.dispatcher.redis.MessageProcessingStrategy;
import be.acerta.webhook.dispatcher.redis.MessageDeliveryType;
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
public class WebhookMessageDeliveryStrategyV1 implements MessageProcessingStrategy {

    @Inject
    private RestTemplate restTemplate;

    @Override
    public boolean canProcess(MessageDeliveryType eventType) {
        return eventType.equals(getProcessedMessageType());
    }

    @Override
    // @Transactional
    public void processMessage(String message) {
        log.debug("processMessage - {} \n", message);
        WebhookMessageDto messageDto = jsonToObject(message, WebhookMessageDto.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("MessageType", messageDto.getType());
        // @fixme pass tracingCorrelationId as header (cfr w3c TraceContext)
        HttpEntity<String> entity = new HttpEntity<>(messageDto.getData(), headers);
        ResponseEntity<String> response = restTemplate.exchange(messageDto.getWebhookUrl(), HttpMethod.POST,
                entity, String.class);
        log.debug("Response status = {}", response.getStatusCode());
    }

    public MessageDeliveryType getProcessedMessageType() {
        return MessageDeliveryType.WEBHOOK_V1;
    }

}
