package fr.ght1pc9kc.baywatch.notify.api.model;

import lombok.Getter;

@Getter
public enum EventType {
    NEWS_ADD("newsAdd"),
    NEWS_UPDATE("newsUpdate"),
    OPEN("open"),
    PING("ping"),
    USER_NOTIFICATION("userNotification"),
    ;

    private final String name;

    EventType(String name) {
        this.name = name;
    }
}
