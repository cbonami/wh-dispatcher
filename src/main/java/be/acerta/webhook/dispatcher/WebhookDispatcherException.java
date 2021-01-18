package be.acerta.webhook.dispatcher;

public class WebhookDispatcherException extends RuntimeException {

    public WebhookDispatcherException() {
    }

    public WebhookDispatcherException(String message) {
        super(message);
    }

    public WebhookDispatcherException(Throwable cause) {
        super(cause);
    }

    public WebhookDispatcherException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebhookDispatcherException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}