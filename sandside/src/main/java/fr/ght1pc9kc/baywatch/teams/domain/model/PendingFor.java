package fr.ght1pc9kc.baywatch.teams.domain.model;

import lombok.Getter;
import lombok.experimental.Accessors;

public enum PendingFor {
    MANAGER(0x01), USER(0x02), NONE(MANAGER.value | USER.value);

    @Getter
    @Accessors(fluent = true)
    private int value;

    PendingFor(int value) {
        this.value = value;
    }

    public static PendingFor from(int value) {
        if (value == MANAGER.value) {
            return MANAGER;
        } else if (value == USER.value) {
            return USER;
        } else if (value == NONE.value) {
            return NONE;
        } else {
            throw new IllegalArgumentException("No PendingFor value for " + value);
        }
    }
}
