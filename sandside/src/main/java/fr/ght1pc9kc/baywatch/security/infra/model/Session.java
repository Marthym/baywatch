package fr.ght1pc9kc.baywatch.security.infra.model;

import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder
@Getter(AccessLevel.NONE)
public class Session {
    public final Entity<User> user;
    public final long maxAge;
}
