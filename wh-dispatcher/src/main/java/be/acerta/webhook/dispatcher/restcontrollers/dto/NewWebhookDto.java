package be.acerta.webhook.dispatcher.restcontrollers.dto;

import java.util.List;

import lombok.*;

@Data
@Builder
@ToString
public class NewWebhookDto {

    @NonNull
    private String url;

    @NonNull
    private String name;

    private boolean pubSub;

    private List<String> subscribesTo;

}