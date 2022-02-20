package fr.ght1pc9kc.baywatch.common.infra.config.jackson.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FeedMixin {
    @JsonIgnore
    Object raw;
}
