package fr.ght1pc9kc.baywatch.notify.api;

public sealed interface ServerEvent<T> permits BasicEvent, ReactiveEvent {
    String id();
    EventType type();
    <R> R accept(ServerEventVisitor<R> visitor);
}
