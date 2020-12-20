package fr.ght1pc9kc.baywatch.api.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class User {
    public @NonNull String id;
    public @NonNull String login;
    public String name;
    public String mail;
}
