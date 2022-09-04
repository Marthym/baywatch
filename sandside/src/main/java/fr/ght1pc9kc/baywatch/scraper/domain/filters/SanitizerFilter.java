package fr.ght1pc9kc.baywatch.scraper.domain.filters;

import fr.ght1pc9kc.baywatch.scraper.api.NewsFilter;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.model.FeedsFilter;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.HtmlSanitizer;
import org.owasp.html.HtmlStreamEventReceiver;
import org.owasp.html.HtmlStreamRenderer;
import org.owasp.html.Sanitizers;
import org.springframework.web.util.HtmlUtils;
import org.unbescape.html.HtmlEscape;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
public class SanitizerFilter implements NewsFilter, FeedsFilter {
    private static final int PLAIN_TEXT_MAX_LENGTH = 250;
    private static final int HTML_MAX_LENGTH = 3_000;

    private static final Function<HtmlStreamEventReceiver, HtmlSanitizer.Policy> TITLE_POLICY =
            new HtmlPolicyBuilder().toFactory();
    private static final Function<HtmlStreamEventReceiver, HtmlSanitizer.Policy> DESCRIPTION_POLICY =
            Sanitizers.FORMATTING;

    @Override
    public Mono<RawNews> filter(@NotNull RawNews news) {
        return Mono.just(news
                .withTitle(sanitizePlainText(news.getTitle()))
                .withDescription(sanitizeHtml(news.getDescription())));
    }

    @Override
    public Mono<AtomFeed> filter(@NotNull AtomFeed feed) {
        return Mono.just(feed.with(
                sanitizePlainText(feed.title()),
                sanitizeHtml(feed.description())
        ));
    }

    private static String sanitizeHtml(final String html) {
        if (html == null || html.isBlank()) {
            return html;
        }

        String htmlEllipsed = html.substring(0, Math.min(HTML_MAX_LENGTH, html.length()));
        StringBuilder htmlBuilder = new StringBuilder();
        HtmlStreamRenderer htmlRenderer = HtmlStreamRenderer.create(htmlBuilder, invalid -> log.trace("Invalid tag detected in description {}", invalid));
        HtmlSanitizer.sanitize(HtmlUtils.htmlUnescape(htmlEllipsed), DESCRIPTION_POLICY.apply(htmlRenderer));
        String saneHtml = htmlBuilder.toString();

        return StringUtils.normalizeSpace(HtmlEscape.unescapeHtml(saneHtml));
    }

    private static String sanitizePlainText(final String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        String txtEllipsed = text.substring(0, Math.min(PLAIN_TEXT_MAX_LENGTH, text.length()));
        StringBuilder txtBuilder = new StringBuilder();
        HtmlStreamRenderer txtRenderer = HtmlStreamRenderer.create(txtBuilder, invalid -> log.trace("Invalid tag detected in title {}", invalid));
        HtmlSanitizer.sanitize(HtmlUtils.htmlUnescape(txtEllipsed), TITLE_POLICY.apply(txtRenderer));
        String saneText = txtBuilder.toString();

        return StringUtils.normalizeSpace(HtmlEscape.unescapeHtml(saneText));
    }
}
