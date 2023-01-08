package fr.ght1pc9kc.baywatch.teams.api.model;

import java.util.Set;

record TeamWithMembers(
        TeamEmpty info,
        Set<String> members
) implements Team {
    @Override
    public String name() {
        return info.name();
    }

    @Override
    public String topic() {
        return info.topic();
    }

    @Override
    public Team withMembers(Set<String> members) {
        return info.withMembers(members);
    }
}
