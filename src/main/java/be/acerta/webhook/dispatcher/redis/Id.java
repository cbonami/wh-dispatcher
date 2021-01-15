package be.acerta.webhook.dispatcher.redis;

import java.io.Serializable;

public interface Id<T> extends Serializable {

    T getValue();

    String asString();
}
