package fr.ght1pc9kc.baywatch.infra.security.model;

import fr.ght1pc9kc.baywatch.api.security.model.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.time.Duration;

@Value
@Builder
@Getter(AccessLevel.NONE)
public class Session {
    public final User user;
    public final long maxAge;
}
