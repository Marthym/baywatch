package fr.ght1pc9kc.baywatch.common.infra.config.jackson.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NewsMixin {
    @JsonIgnore
    Object raw;

    @JsonIgnore
    Object state;
}
