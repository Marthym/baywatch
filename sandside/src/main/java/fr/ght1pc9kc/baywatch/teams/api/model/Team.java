package fr.ght1pc9kc.baywatch.teams.api.model;

import java.util.Set;

public sealed interface Team permits TeamEmpty, TeamWithMembers {
    String name();

    String topic();

    Set<String> members();

    Team withMembers(Set<String> members);

    static Team of(String name, String topic) {
        return new TeamEmpty(name, topic);
    }

    static Team of(String name, String topic, Set<String> members) {
        return new TeamWithMembers(new TeamEmpty(name, topic), members);
    }

    static Team of(Team team, Set<String> members) {
        return team.withMembers(members);
    }
}
