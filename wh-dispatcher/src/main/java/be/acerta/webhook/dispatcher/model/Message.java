package be.acerta.webhook.dispatcher.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.RepresentationModel;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Message extends RepresentationModel<Message> {
    private String id;
    private String idempotencyKey;
    private String data;
    private String delivery;
    private String type;
    private String webhookUrl;
    private String mediaType;


    public static class MessageBuilder {
        private String id;
        private String idempotencyKey;
        private String data;
        private String delivery;
        private String type;
        private String webhookUrl;
        private String mediaType;

        MessageBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public Message.MessageBuilder id(final String id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Message.MessageBuilder idempotencyKey(final String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Message.MessageBuilder data(final String data) {
            this.data = data;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Message.MessageBuilder delivery(final String delivery) {
            this.delivery = delivery;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Message.MessageBuilder type(final String type) {
            this.type = type;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Message.MessageBuilder webhookUrl(final String webhookUrl) {
            this.webhookUrl = webhookUrl;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Message.MessageBuilder mediaType(final String mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public Message build() {
            return new Message(this.id, this.idempotencyKey, this.data, this.delivery, this.type, this.webhookUrl, this.mediaType);
        }

        @Override
        public String toString() {
            return "Message.MessageBuilder(id=" + this.id + ", idempotencyKey=" + this.idempotencyKey + ", data=" + this.data + ", delivery=" + this.delivery + ", type=" + this.type + ", webhookUrl=" + this.webhookUrl + ", mediaType=" + this.mediaType + ")";
        }
    }

    public static Message.MessageBuilder builder() {
        return new Message.MessageBuilder();
    }

    public String getId() {
        return this.id;
    }

    public String getIdempotencyKey() {
        return this.idempotencyKey;
    }

    public String getData() {
        return this.data;
    }

    public String getDelivery() {
        return this.delivery;
    }

    public String getType() {
        return this.type;
    }

    public String getWebhookUrl() {
        return this.webhookUrl;
    }

    public String getMediaType() {
        return this.mediaType;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setIdempotencyKey(final String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public void setData(final String data) {
        this.data = data;
    }

    public void setDelivery(final String delivery) {
        this.delivery = delivery;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setWebhookUrl(final String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void setMediaType(final String mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Message)) return false;
        final Message other = (Message) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$idempotencyKey = this.getIdempotencyKey();
        final Object other$idempotencyKey = other.getIdempotencyKey();
        if (this$idempotencyKey == null ? other$idempotencyKey != null : !this$idempotencyKey.equals(other$idempotencyKey)) return false;
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
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Message;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $idempotencyKey = this.getIdempotencyKey();
        result = result * PRIME + ($idempotencyKey == null ? 43 : $idempotencyKey.hashCode());
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
        return result;
    }

    @Override
    public String toString() {
        return "Message(id=" + this.getId() + ", idempotencyKey=" + this.getIdempotencyKey() + ", data=" + this.getData() + ", delivery=" + this.getDelivery() + ", type=" + this.getType() + ", webhookUrl=" + this.getWebhookUrl() + ", mediaType=" + this.getMediaType() + ")";
    }

    public Message(final String id, final String idempotencyKey, final String data, final String delivery, final String type, final String webhookUrl, final String mediaType) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.data = data;
        this.delivery = delivery;
        this.type = type;
        this.webhookUrl = webhookUrl;
        this.mediaType = mediaType;
    }

    public Message() {
    }
}
