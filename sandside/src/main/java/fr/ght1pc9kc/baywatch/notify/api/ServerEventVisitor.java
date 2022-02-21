package fr.ght1pc9kc.baywatch.notify.api;

public interface ServerEventVisitor<R> {
    <T> R visit(BasicEvent<T> event);

    <T> R visit(ReactiveEvent<T> event);
}
