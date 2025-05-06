package fr.ght1pc9kc.baywatch.scraper.domain.model.ex;

import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import lombok.Getter;

@Getter
public final class FeedScrapingException extends ScrapingException {
    private final transient AtomFeed entity;

    public FeedScrapingException(AtomFeed entity, ScrapingExceptionCode code, Throwable cause) {
        super(code, cause);
        this.entity = entity;
    }
}
