package fr.ght1pc9kc.baywatch.scraper.api.model;

import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.ScrapingException;

import java.util.List;

public record ScrapResult(
        long inserted,
        List<ScrapingException> errors
) {
}
