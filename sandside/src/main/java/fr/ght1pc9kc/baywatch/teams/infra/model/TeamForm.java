package fr.ght1pc9kc.baywatch.teams.infra.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record TeamForm(
        @NotNull @NotEmpty String name,
        String topic
) {
}
