package be.acerta.webhook.dispatcher.vo;

import lombok.*;

@Data
@Builder
public class ApplicationDto {

    @NonNull
    private String url;

    @NonNull
    private String name;

}