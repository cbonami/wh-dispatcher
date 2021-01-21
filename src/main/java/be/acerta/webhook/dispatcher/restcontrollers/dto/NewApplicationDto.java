package be.acerta.webhook.dispatcher.restcontrollers.dto;

import lombok.*;

@Data
@Builder
@ToString
public class NewApplicationDto {

    @NonNull
    private String url;

    @NonNull
    private String name;

}