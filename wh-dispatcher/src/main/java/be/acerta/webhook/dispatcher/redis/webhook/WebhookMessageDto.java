package be.acerta.webhook.dispatcher.redis.webhook;

import java.io.Serializable;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookMessageDto implements Serializable {
    private String id;
    private String data;
    /**
     * how (protocol + agreed post-api) message needs to be delivered, i.e. webhook_v1
     */
    private String delivery;
    /**
     * class of the message i.e. SomethingHappenedEvent
     */
    private String type;
    private String webhookUrl;
    private String mediaType;
    // makes the message unique, so that the reciver can know if it has received the
    // message before when it is being resubmitted
    private String idempotencyKey;


    public static class WebhookMessageDtoBuilder {
        private String id;
        private String data;
        private String delivery;
        private String type;
        private String webhookUrl;
        private String mediaType;
        private String idempotencyKey;

        WebhookMessageDtoBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public WebhookMessageDto.WebhookMessageDtoBuilder id(final String id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WebhookMessageDto.WebhookMessageDtoBuilder data(final String data) {
            this.data = data;
            return this;
        }

        /**
         * how (protocol + agreed post-api) message needs to be delivered, i.e. webhook_v1
         * @return {@code this}.
         */
        public WebhookMessageDto.WebhookMessageDtoBuilder delivery(final String delivery) {
            this.delivery = delivery;
            return this;
        }

        /**
         * class of the message i.e. SomethingHappenedEvent
         * @return {@code this}.
         */
        public WebhookMessageDto.WebhookMessageDtoBuilder type(final String type) {
            this.type = type;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WebhookMessageDto.WebhookMessageDtoBuilder webhookUrl(final String webhookUrl) {
            this.webhookUrl = webhookUrl;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WebhookMessageDto.WebhookMessageDtoBuilder mediaType(final String mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public WebhookMessageDto.WebhookMessageDtoBuilder idempotencyKey(final String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public WebhookMessageDto build() {
            return new WebhookMessageDto(this.id, this.data, this.delivery, this.type, this.webhookUrl, this.mediaType, this.idempotencyKey);
        }

        @Override
        public String toString() {
            return "WebhookMessageDto.WebhookMessageDtoBuilder(id=" + this.id + ", data=" + this.data + ", delivery=" + this.delivery + ", type=" + this.type + ", webhookUrl=" + this.webhookUrl + ", mediaType=" + this.mediaType + ", idempotencyKey=" + this.idempotencyKey + ")";
        }
    }

    public static WebhookMessageDto.WebhookMessageDtoBuilder builder() {
        return new WebhookMessageDto.WebhookMessageDtoBuilder();
    }

    public String getId() {
        return this.id;
    }

    public String getData() {
        return this.data;
    }

    /**
     * how (protocol + agreed post-api) message needs to be delivered, i.e. webhook_v1
     */
    public String getDelivery() {
        return this.delivery;
    }

    /**
     * class of the message i.e. SomethingHappenedEvent
     */
    public String getType() {
        return this.type;
    }

    public String getWebhookUrl() {
        return this.webhookUrl;
    }

    public String getMediaType() {
        return this.mediaType;
    }

    public String getIdempotencyKey() {
        return this.idempotencyKey;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setData(final String data) {
        this.data = data;
    }

    /**
     * how (protocol + agreed post-api) message needs to be delivered, i.e. webhook_v1
     */
    public void setDelivery(final String delivery) {
        this.delivery = delivery;
    }

    /**
     * class of the message i.e. SomethingHappenedEvent
     */
    public void setType(final String type) {
        this.type = type;
    }

    public void setWebhookUrl(final String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void setMediaType(final String mediaType) {
        this.mediaType = mediaType;
    }

    public void setIdempotencyKey(final String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof WebhookMessageDto)) return false;
        final WebhookMessageDto other = (WebhookMessageDto) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$data = this.getData();
        final Object other$data = other.getData();
        if (this$data == null ? other$data != null : !this$data.equals(other$data)) return false;
        final Object this$delivery = this.getDelivery();
        final Object other$delivery = other.getDelivery();
        if (this$delivery == null ? other$delivery != null : !this$delivery.equals(other$delivery)) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        final Object this$webhookUrl = this.getWebhookUrl();
        final Object other$webhookUrl = other.getWebhookUrl();
        if (this$webhookUrl == null ? other$webhookUrl != null : !this$webhookUrl.equals(other$webhookUrl)) return false;
        final Object this$mediaType = this.getMediaType();
        final Object other$mediaType = other.getMediaType();
        if (this$mediaType == null ? other$mediaType != null : !this$mediaType.equals(other$mediaType)) return false;
        final Object this$idempotencyKey = this.getIdempotencyKey();
        final Object other$idempotencyKey = other.getIdempotencyKey();
        if (this$idempotencyKey == null ? other$idempotencyKey != null : !this$idempotencyKey.equals(other$idempotencyKey)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof WebhookMessageDto;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $data = this.getData();
        result = result * PRIME + ($data == null ? 43 : $data.hashCode());
        final Object $delivery = this.getDelivery();
        result = result * PRIME + ($delivery == null ? 43 : $delivery.hashCode());
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final Object $webhookUrl = this.getWebhookUrl();
        result = result * PRIME + ($webhookUrl == null ? 43 : $webhookUrl.hashCode());
        final Object $mediaType = this.getMediaType();
        result = result * PRIME + ($mediaType == null ? 43 : $mediaType.hashCode());
        final Object $idempotencyKey = this.getIdempotencyKey();
        result = result * PRIME + ($idempotencyKey == null ? 43 : $idempotencyKey.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "WebhookMessageDto(id=" + this.getId() + ", data=" + this.getData() + ", delivery=" + this.getDelivery() + ", type=" + this.getType() + ", webhookUrl=" + this.getWebhookUrl() + ", mediaType=" + this.getMediaType() + ", idempotencyKey=" + this.getIdempotencyKey() + ")";
    }

    public WebhookMessageDto() {
    }

    public WebhookMessageDto(final String id, final String data, final String delivery, final String type, final String webhookUrl, final String mediaType, final String idempotencyKey) {
        this.id = id;
        this.data = data;
        this.delivery = delivery;
        this.type = type;
        this.webhookUrl = webhookUrl;
        this.mediaType = mediaType;
        this.idempotencyKey = idempotencyKey;
    }
}
