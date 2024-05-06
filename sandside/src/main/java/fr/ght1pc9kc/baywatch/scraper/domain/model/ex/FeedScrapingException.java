package fr.ght1pc9kc.baywatch.scraper.domain.model.ex;

import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import lombok.Getter;

@Getter
public final class FeedScrapingException extends ScrapingException {
    private final transient AtomFeed entity;

    public FeedScrapingException(AtomFeed entity, Throwable cause) {
        super((cause != null) ? cause.getLocalizedMessage() : "null", cause);
        this.entity = entity;
    }
}
