package fr.ght1pc9kc.baywatch.teams.api.model;

import fr.ght1pc9kc.baywatch.teams.domain.model.PendingFor;

public record TeamMember(String userId, PendingFor pending) {
}
