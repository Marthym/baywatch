package fr.ght1pc9kc.baywatch.scraper.domain.model;

import java.time.Duration;
import java.time.Period;

public interface ScraperProperties {
    Duration frequency();

    Period conservation();
}
