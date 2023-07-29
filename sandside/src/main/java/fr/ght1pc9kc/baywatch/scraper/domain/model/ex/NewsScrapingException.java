package fr.ght1pc9kc.baywatch.scraper.domain.model.ex;

import fr.ght1pc9kc.baywatch.scraper.api.model.AtomEntry;
import lombok.Getter;

public class NewsScrapingException extends ScrapingException {
    @Getter
    private final transient AtomEntry entity;

    public NewsScrapingException(AtomEntry entity, Throwable cause) {
        super(cause.getLocalizedMessage(), cause);
        this.entity = entity;
    }
}
