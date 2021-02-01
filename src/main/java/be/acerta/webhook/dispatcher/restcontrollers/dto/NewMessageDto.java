package be.acerta.webhook.dispatcher.restcontrollers.dto;

import lombok.*;

@Data
@Builder
public class NewMessageDto {

    private String type;
    private String data;
    
}
