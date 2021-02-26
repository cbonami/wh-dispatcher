package be.acerta.webhook.dispatcher.restcontrollers.dto;

import java.io.Serializable;

public class BooleanDto implements Serializable {
    private static final long serialVersionUID = -2034514381667095151L;
    private boolean value;

    public static BooleanDto booleanDto() {
        return new BooleanDto();
    }

    public BooleanDto withValue(boolean value) {
        this.value = value;
        return this;
    }

    public BooleanDto() {
    }

    public boolean isValue() {
        return this.value;
    }

    public void setValue(final boolean value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BooleanDto)) return false;
        final BooleanDto other = (BooleanDto) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.isValue() != other.isValue()) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BooleanDto;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isValue() ? 79 : 97);
        return result;
    }

    @Override
    public String toString() {
        return "BooleanDto(value=" + this.isValue() + ")";
    }
}
