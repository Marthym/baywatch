package fr.ght1pc9kc.baywatch.notify.api.model;

public sealed interface ServerEvent permits BasicEvent, ReactiveEvent {
    String id();

    EventType type();

    <R> R accept(ServerEventVisitor<R> visitor);
}
