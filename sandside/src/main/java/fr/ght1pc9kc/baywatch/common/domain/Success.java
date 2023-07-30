package fr.ght1pc9kc.baywatch.common.domain;

import java.util.function.Supplier;

public record Success<T>(T value) implements Try<T> {
    @Override
    public T get() {
        return value;
    }

    @Override
    public boolean isFailure() {
        return false;
    }

    @Override
    public Throwable getCause() {
        throw new IllegalStateException("Try is not a failure !");
    }

    @Override
    public T orElse(T failover) {
        return value;
    }

    @Override
    public T orElseGet(Supplier<T> supplier) {
        return value;
    }
}
