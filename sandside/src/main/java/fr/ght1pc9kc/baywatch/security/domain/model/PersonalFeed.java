package fr.ght1pc9kc.baywatch.security.domain.model;

import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.entity.api.Entity;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public record PersonalFeed(
        String id,
        String name,
        String description,
        URI icon,
        URI location,
        boolean visible
) {
    public static PersonalFeed of(Entity<User> user) {
        URI icon;
        try {
            @SuppressWarnings("java:S4790")
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(user.self().mail().getBytes());
            String gravatar = HexFormat.of().formatHex(md5.digest());
            icon = URI.create("https://www.gravatar.com/avatar/" + gravatar + "?s=96&d=retro");
        } catch (NoSuchAlgorithmException e) {
            icon = URI.create("https://www.gravatar.com/avatar/" + user.id() + "?s=96&d=retro");
        }
        return new PersonalFeed(
                user.id(), user.self().name() + "(personal)",
                "Personal Baywatch pocket for you",
                icon, URI.create("https://localhost/" + user.id()),
                false);
    }
}
