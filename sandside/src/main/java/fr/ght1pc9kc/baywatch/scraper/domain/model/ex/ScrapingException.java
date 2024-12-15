package fr.ght1pc9kc.baywatch.scraper.domain.model.ex;

import lombok.Getter;

@Getter
public sealed class ScrapingException extends RuntimeException permits FeedScrapingException, NewsScrapingException {
    private final ScrapingExceptionCode translation;

    public ScrapingException(String message, Throwable cause) {
        super(message, cause);
        this.translation = ScrapingExceptionCode.DEFAULT;
    }

    public ScrapingException(ScrapingExceptionCode code, Throwable cause) {
        super(code.getDefaultMessage(), cause);
        this.translation = code;
    }
}
