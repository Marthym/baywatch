package fr.ght1pc9kc.baywatch.security.api.model;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;

import java.util.Collection;

public record BaywatchAuthentication(
        Entity<User> user,
        String token,
        boolean rememberMe,
        Collection<String> authorities
) {
}
