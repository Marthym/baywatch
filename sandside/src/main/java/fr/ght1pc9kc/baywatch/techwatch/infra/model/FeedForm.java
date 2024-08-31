package fr.ght1pc9kc.baywatch.techwatch.infra.model;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import java.util.Set;

public record FeedForm(
        @NotBlank String name,
        @NotBlank @URL String location,
        String description,
        Set<String> tags
) {
}
