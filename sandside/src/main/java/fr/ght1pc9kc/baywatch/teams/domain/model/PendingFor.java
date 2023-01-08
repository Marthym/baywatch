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
}
