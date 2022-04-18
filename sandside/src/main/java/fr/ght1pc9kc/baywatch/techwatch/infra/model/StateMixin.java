package fr.ght1pc9kc.baywatch.techwatch.infra.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class StateMixin {
    @JsonIgnore
    public int flags;
}
