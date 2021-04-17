package fr.ght1pc9kc.baywatch.infra.config.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FeedMixin {
    @JsonIgnore
    Object raw;
}
