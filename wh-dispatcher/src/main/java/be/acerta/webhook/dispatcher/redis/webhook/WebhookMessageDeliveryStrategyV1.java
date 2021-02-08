package be.acerta.webhook.dispatcher.redis.webhook;

import static be.acerta.webhook.dispatcher.redis.JsonUtil.jsonToObject;

import java.util.Collections;

import javax.inject.Inject;

import be.acerta.webhook.dispatcher.redis.MessageDeliveryType;
import be.acerta.webhook.dispatcher.redis.MessageProcessingStrategy;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Here we POST the message's data in a way that we consider to be the 'version 1' way of delivering data.
 * For example, in the 'contract' we stated the the message's type (class) is passed as a header ("MessageType").
 * 'version 2' might never come, but we keep our options open.
 */
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
    @Timed("acerta.webhook.invocation")
    // @Transactional
    public void processMessage(String message) {
        log.debug("processMessage - {} \n", message);
        WebhookMessageDto messageDto = jsonToObject(message, WebhookMessageDto.class);
        HttpHeaders headers = new HttpHeaders();
        // @fixme use mediatype instead of mimetype
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
