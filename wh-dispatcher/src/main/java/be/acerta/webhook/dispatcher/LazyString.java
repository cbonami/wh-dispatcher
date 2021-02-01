package be.acerta.webhook.dispatcher;

import java.util.function.Supplier;

// https://itnext.io/lazy-logging-40314cf9bb25
public class LazyString {

    private final Supplier<?> stringSupplier;

    public static LazyString lazy(Supplier<?> stringSupplier) {
        return new LazyString(stringSupplier);
    }

    public LazyString(final Supplier<?> stringSupplier) {
        this.stringSupplier = stringSupplier;
    }

    @Override
    public String toString() {
        return String.valueOf(stringSupplier.get());
    }
}