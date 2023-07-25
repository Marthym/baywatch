package fr.ght1pc9kc.baywatch.common.domain;

public record Success<T>(
        T value
) implements Try<T> {
}
