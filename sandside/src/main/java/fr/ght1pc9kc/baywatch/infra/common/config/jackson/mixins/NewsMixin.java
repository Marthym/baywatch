package fr.ght1pc9kc.baywatch.infra.common.config.jackson.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NewsMixin {
    @JsonIgnore
    Object raw;

    @JsonIgnore
    Object state;
}
