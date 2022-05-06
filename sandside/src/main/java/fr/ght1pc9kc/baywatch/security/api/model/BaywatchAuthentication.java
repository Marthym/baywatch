package fr.ght1pc9kc.baywatch.security.api.model;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import lombok.Value;

import java.util.Collection;

@Value
public class BaywatchAuthentication {
    public Entity<User> user;
    public String token;
    public boolean rememberMe;
    public Collection<String> authorities;
}
