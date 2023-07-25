package fr.ght1pc9kc.baywatch.common.domain;

public record Failure<T>(Throwable e) implements Try<T> {
}
