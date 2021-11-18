package fr.ght1pc9kc.baywatch.api.notify;

import lombok.Getter;

public enum EventType {
    NEWS("news");

    @Getter
    private final String name;

    EventType(String name) {
        this.name = name;
    }
}
