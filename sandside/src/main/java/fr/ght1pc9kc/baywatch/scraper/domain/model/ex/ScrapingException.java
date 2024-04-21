package fr.ght1pc9kc.baywatch.scraper.domain.model.ex;

public sealed class ScrapingException extends RuntimeException permits FeedScrapingException, NewsScrapingException{
    public ScrapingException(String message, Throwable cause) {
        super(message, cause);
    }
}
