package fr.ght1pc9kc.baywatch.admin.domain.samples;

import fr.ght1pc9kc.entity.api.Entity;

import java.util.List;
import java.util.Map;

public class AppConfigSamples {
    public static final List<Entity<Map.Entry<String, String>>> STAR_WARS_PARAMS = List.of(
            Entity.identify(Map.entry("jedi.master", "Yoda")).withId("CF01K2F88DYNCAPEAKCZEWXY3Z7N"),
            Entity.identify(Map.entry("jedi.knight", "Luke Skywalker")).withId("CF01K2F88EB6VJMH79EGGTTED040"),
            Entity.identify(Map.entry("sith.lord", "Darth Vader")).withId("CF01K2F88ERJ2FZ7EQSXVM8XVWT0"),
            Entity.identify(Map.entry("saber.color", "green")).withId("CF01K2F88FG9WNNWED3P57WCE125"),
            Entity.identify(Map.entry("deathstar.power", "over-9000")).withId("CF01K2F88G9XYWS9PXC4Y4X0EQAR")
    );
}
