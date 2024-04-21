package fr.ght1pc9kc.baywatch.scraper.domain.model.ex;

import fr.ght1pc9kc.baywatch.scraper.api.model.AtomEntry;
import lombok.Getter;

@Getter
public final class NewsScrapingException extends ScrapingException {
    private final transient AtomEntry entity;

    public NewsScrapingException(AtomEntry entity, Throwable cause) {
        super(cause.getLocalizedMessage(), cause);
        this.entity = entity;
    }
}
