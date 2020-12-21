package fr.ght1pc9kc.baywatch.api.model;

import lombok.*;

@Value
@Builder
@Getter(AccessLevel.NONE)
public class User {
    public @NonNull String id;
    public @NonNull String login;
    public String name;
    public String mail;
}
