package fr.ght1pc9kc.baywatch.infra.security.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserMixin {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
