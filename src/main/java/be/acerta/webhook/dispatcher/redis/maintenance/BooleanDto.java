package be.acerta.webhook.dispatcher.redis.maintenance;

import java.io.Serializable;

import lombok.Data;

@Data
public class BooleanDto implements Serializable {

    private static final long serialVersionUID = -2034514381667095151L;

    private boolean value;

    private BooleanDto() {
    }

    public static BooleanDto booleanDto() {
        return new BooleanDto();
    }

    public BooleanDto withValue(boolean value) {
        this.value = value;
        return this;
    }

}
