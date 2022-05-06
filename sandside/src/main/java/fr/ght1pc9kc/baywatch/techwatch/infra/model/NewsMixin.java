package fr.ght1pc9kc.baywatch.techwatch.infra.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NewsMixin {
    @JsonIgnore
    Object raw;

    @JsonIgnore
    Object state;
}
