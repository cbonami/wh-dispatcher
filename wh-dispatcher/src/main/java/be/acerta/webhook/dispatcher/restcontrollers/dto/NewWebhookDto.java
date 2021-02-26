package be.acerta.webhook.dispatcher.restcontrollers.dto;

import java.util.List;


public class NewWebhookDto {

    private String url;

    private String name;
    private boolean pubSub;
    private List<String> subscribesTo;

    NewWebhookDto(final String url, final String name, final boolean pubSub, final List<String> subscribesTo) {
        if (url == null) {
            throw new NullPointerException("url is marked non-null but is null");
        }
        if (name == null) {
            throw new NullPointerException("name is marked non-null but is null");
        }
        this.url = url;
        this.name = name;
        this.pubSub = pubSub;
        this.subscribesTo = subscribesTo;
    }


    public static class NewWebhookDtoBuilder {
        private String url;
        private String name;
        private boolean pubSub;
        private List<String> subscribesTo;

        NewWebhookDtoBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public NewWebhookDto.NewWebhookDtoBuilder url( final String url) {
            if (url == null) {
                throw new NullPointerException("url is marked non-null but is null");
            }
            this.url = url;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public NewWebhookDto.NewWebhookDtoBuilder name( final String name) {
            if (name == null) {
                throw new NullPointerException("name is marked non-null but is null");
            }
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public NewWebhookDto.NewWebhookDtoBuilder pubSub(final boolean pubSub) {
            this.pubSub = pubSub;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public NewWebhookDto.NewWebhookDtoBuilder subscribesTo(final List<String> subscribesTo) {
            this.subscribesTo = subscribesTo;
            return this;
        }

        public NewWebhookDto build() {
            return new NewWebhookDto(this.url, this.name, this.pubSub, this.subscribesTo);
        }

        @Override
        public String toString() {
            return "NewWebhookDto.NewWebhookDtoBuilder(url=" + this.url + ", name=" + this.name + ", pubSub=" + this.pubSub + ", subscribesTo=" + this.subscribesTo + ")";
        }
    }

    public static NewWebhookDto.NewWebhookDtoBuilder builder() {
        return new NewWebhookDto.NewWebhookDtoBuilder();
    }


    public String getUrl() {
        return this.url;
    }


    public String getName() {
        return this.name;
    }

    public boolean isPubSub() {
        return this.pubSub;
    }

    public List<String> getSubscribesTo() {
        return this.subscribesTo;
    }

    public void setUrl(final String url) {
        if (url == null) {
            throw new NullPointerException("url is marked non-null but is null");
        }
        this.url = url;
    }

    public void setName(final String name) {
        if (name == null) {
            throw new NullPointerException("name is marked non-null but is null");
        }
        this.name = name;
    }

    public void setPubSub(final boolean pubSub) {
        this.pubSub = pubSub;
    }

    public void setSubscribesTo(final List<String> subscribesTo) {
        this.subscribesTo = subscribesTo;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof NewWebhookDto)) return false;
        final NewWebhookDto other = (NewWebhookDto) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.isPubSub() != other.isPubSub()) return false;
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
        return other instanceof NewWebhookDto;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isPubSub() ? 79 : 97);
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
        return "NewWebhookDto(url=" + this.getUrl() + ", name=" + this.getName() + ", pubSub=" + this.isPubSub() + ", subscribesTo=" + this.getSubscribesTo() + ")";
    }
}
