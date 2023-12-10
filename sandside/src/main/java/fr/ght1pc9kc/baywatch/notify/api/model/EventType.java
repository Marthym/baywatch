package fr.ght1pc9kc.baywatch.notify.api.model;

import lombok.Getter;

@Getter
public enum EventType {
    NEWS_UPDATE("newsUpdate"),
    USER_NOTIFICATION("userNotification"),
    NEWS_ADD("newsAdd");

    private final String name;

    EventType(String name) {
        this.name = name;
    }
}
