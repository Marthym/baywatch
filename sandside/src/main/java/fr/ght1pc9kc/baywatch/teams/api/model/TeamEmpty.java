package fr.ght1pc9kc.baywatch.teams.api.model;

import java.util.Set;

record TeamEmpty(String name, String topic) implements Team {
    @Override
    public Set<String> members() {
        return Set.of();
    }

    public Team withMembers(Set<String> members) {
        return new TeamWithMembers(this, members);
    }
}
