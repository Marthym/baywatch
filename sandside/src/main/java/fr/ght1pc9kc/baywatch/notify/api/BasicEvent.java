package fr.ght1pc9kc.baywatch.notify.api;

public record BasicEvent<T>(
        String id,
        EventType type,
        T message
) implements ServerEvent<T> {
    @Override
    public <R> R accept(ServerEventVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
