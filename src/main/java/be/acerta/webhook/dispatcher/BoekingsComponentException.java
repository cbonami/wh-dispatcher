package be.acerta.webhook.dispatcher;

import java.util.UUID;

public class BoekingsComponentException extends RuntimeException {

    private final String id;

    protected BoekingsComponentException(String message, Throwable throwable, String id) {
        super(message, throwable);
        this.id = id;
    }

    protected BoekingsComponentException(String message, Throwable throwable) {
        super(message, throwable);
        this.id = UUID.randomUUID().toString();
    }

    protected BoekingsComponentException(String message, String id) {
        super(message);
        this.id = id;
    }

    protected BoekingsComponentException(String message) {
        super(message);
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public static BoekingsComponentException boekingscomponentException(Throwable throwable) {
        return new BoekingsComponentException(throwable.getMessage(), throwable);
    }

    public static BoekingsComponentException boekingscomponentException(String message, Throwable throwable) {
        return new BoekingsComponentException(message, throwable);
    }

    public static BoekingsComponentException boekingscomponentException(String message) {
        return new BoekingsComponentException(message);
    }
}
