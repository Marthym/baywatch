package fr.ght1pc9kc.baywatch.teams.api.model;

import java.util.Set;

public record Team(
        String name,
        Set<String> members
) {
}
