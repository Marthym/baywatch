package fr.ght1pc9kc.baywatch.common.domain;

import lombok.SneakyThrows;

import java.util.function.Supplier;

public record Failure<T>(Throwable e) implements Try<T> {
    @Override
    @SneakyThrows
    public T get() {
        throw e;
    }

    @Override
    public boolean isFailure() {
        return true;
    }

    @Override
    public Throwable getCause() {
        return e;
    }

    @Override
    public T orElse(T failover) {
        return failover;
    }

    @Override
    public T orElseGet(Supplier<T> supplier) {
        return supplier.get();
    }
}
