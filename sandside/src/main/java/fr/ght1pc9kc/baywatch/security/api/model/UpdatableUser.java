package fr.ght1pc9kc.baywatch.security.api.model;

import lombok.Builder;

import java.util.List;
import java.util.Optional;

@Builder(toBuilder = true)
public record UpdatableUser(
        String login,
        Optional<String> name,
        String mail,
        String password,
        List<Permission> roles
) {
}
