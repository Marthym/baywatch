package fr.ght1pc9kc.baywatch.scrapper.domain.model;

import java.time.Duration;
import java.time.Period;

public record ScraperConfig(
        Duration frequency,
        Period conservation
) {
}
