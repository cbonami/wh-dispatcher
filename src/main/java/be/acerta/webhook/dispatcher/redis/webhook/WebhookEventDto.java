package be.acerta.webhook.dispatcher.redis.webhook;

import be.acerta.webhook.dispatcher.redis.dto.Dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class WebhookEventDto extends Dto {

    public Long id;
    public String bucketId;
    public String jsonData;
    public String eventType;

    /*
     * private WebhookEventDto() { }
     */

    /*
     * private WebhookEventDto(SkedifyEventDtoBuilder builder) { this.id =
     * builder.id; this.bucketId = builder.bucketId; this.jsonData =
     * builder.jsonData; this.eventType = builder.eventType; }
     */

    /*
     * public static class SkedifyEventDtoBuilder extends Builder<SkedifyEventDto> {
     * 
     * @NotNull private Long id;
     * 
     * @NotNull private String bucketId;
     * 
     * @NotNull private String jsonData;
     * 
     * @NotNull private String eventType;
     * 
     * public static SkedifyEventDtoBuilder skedifyEventDto() { return new
     * SkedifyEventDtoBuilder(); }
     * 
     * @Override protected SkedifyEventDto buildInternal() { return new
     * SkedifyEventDto(this); }
     * 
     * public SkedifyEventDtoBuilder withId(Long id) { this.id = id; return this; }
     * 
     * public SkedifyEventDtoBuilder withBucketId(String bucketId) { this.bucketId =
     * bucketId; return this; }
     * 
     * public SkedifyEventDtoBuilder withJsonData(String jsonData) { this.jsonData =
     * jsonData; return this; }
     * 
     * public SkedifyEventDtoBuilder withEventType(String eventType) {
     * this.eventType = eventType; return this; } }
     */
}
