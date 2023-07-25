package fr.ght1pc9kc.baywatch.common.domain;

import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Try<T> permits Success, Failure {
    static <T> Try<T> of(Supplier<? extends T> supplier) {
        try {
            return Try.success(supplier.get());
        } catch (Exception e) {
            return Try.fail(e);
        }
    }

    static <E, T> Function<E, Try<T>> of(Function<E, ? extends T> func) {
        return input -> Try.of(() -> func.apply(input));
    }

    static <T> Try<T> success(T value) {
        return new Success<>(value);
    }

    static <T> Try<T> fail(Throwable e) {
        return new Failure<>(e);
    }
}
