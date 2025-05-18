package fr.ght1pc9kc.baywatch.scraper.domain.model.ex;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.logging.Level;

@Getter
@RequiredArgsConstructor
public enum ScrapingExceptionCode {
    UNKNOWN(418, Level.WARNING, "sandside.scraping.default"),
    PARSING(422, Level.WARNING, "sandside.scraping.parsing"),
    NOT_FOUND(404, Level.SEVERE, "sandside.scraping.notFound"),
    NEED_ACCOUNT(403, Level.WARNING, "sandside.scraping.needAccount"),
    UNSUPPORTED(415, Level.WARNING, "sandside.scraping.unsupported"),
    TIMEOUT(408, Level.WARNING, "sandside.scraping.timeout"),
    GONE(410, Level.WARNING, "sandside.scraping.gone"),
    UNAVAILABLE(503, Level.SEVERE, "sandside.scraping.unavailable"),
    ;

    private static final ScrapingExceptionCode[] VALUES = ScrapingExceptionCode.values();

    private final int httpStatusCode;
    private final Level level;
    private final String messageKey;

    public static ScrapingExceptionCode fromHttpStatus(int httpStatusCode) {
        for (ScrapingExceptionCode code : VALUES) {
            if (code.httpStatusCode == httpStatusCode) {
                return code;
            }
        }
        return UNKNOWN;
    }
}
