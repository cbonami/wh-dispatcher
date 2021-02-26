package be.acerta.webhook.dispatcher.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.springframework.hateoas.RepresentationModel;

/**
 * A mini-queue for e.g. a specific person. All messages in the bucket will be
 * delivered in fixed order.
 */
@JsonInclude(Include.NON_NULL)
public class Bucket extends RepresentationModel<Bucket> {
    private String id;
    private List<Message> messages;


    public static class BucketBuilder {
        private String id;
        private List<Message> messages;

        BucketBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public Bucket.BucketBuilder id(final String id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Bucket.BucketBuilder messages(final List<Message> messages) {
            this.messages = messages;
            return this;
        }

        public Bucket build() {
            return new Bucket(this.id, this.messages);
        }

        @Override
        public String toString() {
            return "Bucket.BucketBuilder(id=" + this.id + ", messages=" + this.messages + ")";
        }
    }

    public static Bucket.BucketBuilder builder() {
        return new Bucket.BucketBuilder();
    }

    public String getId() {
        return this.id;
    }

    public List<Message> getMessages() {
        return this.messages;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setMessages(final List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Bucket)) return false;
        final Bucket other = (Bucket) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$messages = this.getMessages();
        final Object other$messages = other.getMessages();
        if (this$messages == null ? other$messages != null : !this$messages.equals(other$messages)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Bucket;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $messages = this.getMessages();
        result = result * PRIME + ($messages == null ? 43 : $messages.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Bucket(id=" + this.getId() + ", messages=" + this.getMessages() + ")";
    }

    public Bucket(final String id, final List<Message> messages) {
        this.id = id;
        this.messages = messages;
    }

    public Bucket() {
    }
}
