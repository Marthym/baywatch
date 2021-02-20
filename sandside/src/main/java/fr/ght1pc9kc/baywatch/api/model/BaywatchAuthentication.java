package fr.ght1pc9kc.baywatch.api.model;

import lombok.Value;

import java.util.Collection;

@Value
public class BaywatchAuthentication {
    public User user;
    public String token;
    public Collection<String> authorities;
}
