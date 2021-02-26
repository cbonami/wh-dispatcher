package be.acerta.webhook.dispatcher.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.hateoas.RepresentationModel;

/**
 * From a more physical point of view, a ```Webhook``` represents an endpoint
 * (url) that the dispatcher will POST messages to. However, multiple
 * Webhook-instances can share the same url. The latter means that, from a
 * logical point of view, a ```Webhook``` can also be seen as a named
 * point-to-point 'Queue' from a (set of systems) to a particular other system.
 */
@RedisHash("Webhook")
@JsonInclude(Include.NON_NULL)
public class Webhook extends RepresentationModel<Webhook> {
    @Id
    private String id;
    private String url;
    @Indexed
    private String name;
    @Indexed
    private Boolean pubSub;
    /**
     * MessageTypes that this webhook subscribes to.
     */
    private List<String> subscribesTo;


    public static class WebhookBuilder {
        private String id;
        private String url;
        private String name;
        private Boolean pubSub;
        private List<String> subscribesTo;

        WebhookBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public Webhook.WebhookBuilder id(final String id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Webhook.WebhookBuilder url(final String url) {
            this.url = url;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Webhook.WebhookBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Webhook.WebhookBuilder pubSub(final Boolean pubSub) {
            this.pubSub = pubSub;
            return this;
        }

        /**
         * MessageTypes that this webhook subscribes to.
         * @return {@code this}.
         */
        public Webhook.WebhookBuilder subscribesTo(final List<String> subscribesTo) {
            this.subscribesTo = subscribesTo;
            return this;
        }

        public Webhook build() {
            return new Webhook(this.id, this.url, this.name, this.pubSub, this.subscribesTo);
        }

        @Override
        public String toString() {
            return "Webhook.WebhookBuilder(id=" + this.id + ", url=" + this.url + ", name=" + this.name + ", pubSub=" + this.pubSub + ", subscribesTo=" + this.subscribesTo + ")";
        }
    }

    public static Webhook.WebhookBuilder builder() {
        return new Webhook.WebhookBuilder();
    }

    public String getId() {
        return this.id;
    }

    public String getUrl() {
        return this.url;
    }

    public String getName() {
        return this.name;
    }

    public Boolean getPubSub() {
        return this.pubSub;
    }

    /**
     * MessageTypes that this webhook subscribes to.
     */
    public List<String> getSubscribesTo() {
        return this.subscribesTo;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setPubSub(final Boolean pubSub) {
        this.pubSub = pubSub;
    }

    /**
     * MessageTypes that this webhook subscribes to.
     */
    public void setSubscribesTo(final List<String> subscribesTo) {
        this.subscribesTo = subscribesTo;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Webhook)) return false;
        final Webhook other = (Webhook) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$pubSub = this.getPubSub();
        final Object other$pubSub = other.getPubSub();
        if (this$pubSub == null ? other$pubSub != null : !this$pubSub.equals(other$pubSub)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$url = this.getUrl();
        final Object other$url = other.getUrl();
        if (this$url == null ? other$url != null : !this$url.equals(other$url)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$subscribesTo = this.getSubscribesTo();
        final Object other$subscribesTo = other.getSubscribesTo();
        if (this$subscribesTo == null ? other$subscribesTo != null : !this$subscribesTo.equals(other$subscribesTo)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Webhook;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $pubSub = this.getPubSub();
        result = result * PRIME + ($pubSub == null ? 43 : $pubSub.hashCode());
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $url = this.getUrl();
        result = result * PRIME + ($url == null ? 43 : $url.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $subscribesTo = this.getSubscribesTo();
        result = result * PRIME + ($subscribesTo == null ? 43 : $subscribesTo.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Webhook(id=" + this.getId() + ", url=" + this.getUrl() + ", name=" + this.getName() + ", pubSub=" + this.getPubSub() + ", subscribesTo=" + this.getSubscribesTo() + ")";
    }

    public Webhook() {
    }

    public Webhook(final String id, final String url, final String name, final Boolean pubSub, final List<String> subscribesTo) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.pubSub = pubSub;
        this.subscribesTo = subscribesTo;
    }
    // @Indexed
    // private Boolean online;
}
