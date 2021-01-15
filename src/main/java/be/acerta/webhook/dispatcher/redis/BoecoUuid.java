package be.acerta.webhook.dispatcher.redis;

import com.google.common.base.Preconditions;

import javax.validation.constraints.NotNull;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class BoecoUuid extends ValueObject implements Id<String> {

    @NotNull
    private final String value;

    protected BoecoUuid() {
        this.value = null;
    }

    protected BoecoUuid(String value) {
        this.value = value;
        Preconditions.checkArgument(isNotBlank(value), "Value moet ingevuld zijn.");
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String asString() {
        return value;
    }
}