package be.acerta.webhook.dispatcher.restcontrollers.dto;


public class NewMessageDto {
    private String type;
    private String data;

    NewMessageDto(final String type, final String data) {
        this.type = type;
        this.data = data;
    }


    public static class NewMessageDtoBuilder {
        private String type;
        private String data;

        NewMessageDtoBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public NewMessageDto.NewMessageDtoBuilder type(final String type) {
            this.type = type;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public NewMessageDto.NewMessageDtoBuilder data(final String data) {
            this.data = data;
            return this;
        }

        public NewMessageDto build() {
            return new NewMessageDto(this.type, this.data);
        }

        @Override
        public String toString() {
            return "NewMessageDto.NewMessageDtoBuilder(type=" + this.type + ", data=" + this.data + ")";
        }
    }

    public static NewMessageDto.NewMessageDtoBuilder builder() {
        return new NewMessageDto.NewMessageDtoBuilder();
    }

    public String getType() {
        return this.type;
    }

    public String getData() {
        return this.data;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setData(final String data) {
        this.data = data;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof NewMessageDto)) return false;
        final NewMessageDto other = (NewMessageDto) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        final Object this$data = this.getData();
        final Object other$data = other.getData();
        if (this$data == null ? other$data != null : !this$data.equals(other$data)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof NewMessageDto;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final Object $data = this.getData();
        result = result * PRIME + ($data == null ? 43 : $data.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "NewMessageDto(type=" + this.getType() + ", data=" + this.getData() + ")";
    }
}
