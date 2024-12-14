package fr.ght1pc9kc.baywatch.scraper.domain.model.ex;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScrapingExceptionCode {
    DEFAULT("sandside.scraping.default", "Unknown error from feed scraping"),
    PARSING("sandside.scraping.parsing", "Error on parsing feed flux. Will be fixed soon."),
    NOT_FOUND("sandside.scraping.notFound", "Feed not found."),
    NEED_ACCOUNT("sandside.scraping.needAccount", "Feed expect credentials to be read"),
    UNSUPPORTED("sandside.scraping.unsupported", "Feed format unknown and not supported."),
    GONE("sandside.scraping.gone", "Feed is gone for ever, you can remove it !"),
    UNAVAILABLE("sandside.scraping.unavailable", "Feed unavailable"),
    DONE("sandside.scraping.done", "Feed server is done"),
    ;

    private final String code;
    private final String defaultMessage;

    public static ScrapingExceptionCode fromHttpStatus(int httpStatusCode) {
        return switch (httpStatusCode) {
            case 200, 599 -> PARSING;
            case 403 -> NEED_ACCOUNT;
            case 404 -> NOT_FOUND;
            case 406 -> UNSUPPORTED;
            case 410 -> GONE;
            case 500 -> UNAVAILABLE;
            case 521 -> DONE;
            default -> DEFAULT;
        };
    }
}
