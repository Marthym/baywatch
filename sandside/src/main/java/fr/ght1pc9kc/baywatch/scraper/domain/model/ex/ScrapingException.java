package fr.ght1pc9kc.baywatch.scraper.domain.model.ex;

public class ScrapingException extends RuntimeException {
    public ScrapingException(String message, Throwable cause) {
        super(message, cause);
    }
}
