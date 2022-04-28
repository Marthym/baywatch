package fr.ght1pc9kc.baywatch.scrapper.domain.plugins;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.scrapper.api.FeedParserPlugin;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.HtmlSanitizer;
import org.owasp.html.HtmlStreamEventReceiver;
import org.owasp.html.HtmlStreamRenderer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Nonnull;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
@Component
public final class DefaultParserPlugin implements FeedParserPlugin {
    private static final int DESCRIPTION_MAX_LENGTH = 3_000;

    private static final Function<HtmlStreamEventReceiver, HtmlSanitizer.Policy> POLICY = new HtmlPolicyBuilder()
            .allowElements("a", "p", "ul", "li", "pre")
            .allowAttributes("href").onElements("a")
            .toFactory();

    @Override
    public String pluginForDomain() {
        return "*";
    }

    @Override
    public RawNews.RawNewsBuilder handleItemEvent() {
        return RawNews.builder();
    }

    @Override
    public RawNews.RawNewsBuilder handleLinkEvent(@Nonnull RawNews.RawNewsBuilder builder, URI link) {
        return builder.id(Hasher.identify(link))
                .link(link);
    }

    @Override
    public RawNews.RawNewsBuilder handleDescriptionEvent(@NotNull RawNews.RawNewsBuilder builder, String content) {
        if (Objects.isNull(content)) {
            return FeedParserPlugin.super.handleDescriptionEvent(builder, "");
        }
        if (content.isBlank()) {
            return FeedParserPlugin.super.handleDescriptionEvent(builder, content);
        }
        String ellipsed = content.substring(0, Math.min(DESCRIPTION_MAX_LENGTH, content.length() - 1));
        StringBuilder finalHtml = new StringBuilder();
        HtmlStreamRenderer renderer = HtmlStreamRenderer.create(finalHtml, invalid -> log.trace("Invalid tag detected {}", invalid));
        HtmlSanitizer.sanitize(HtmlUtils.htmlUnescape(ellipsed), POLICY.apply(renderer));
        String reEncodedHtml = HtmlUtils.htmlEscape(finalHtml.toString(), StandardCharsets.UTF_8.name());
        return FeedParserPlugin.super.handleDescriptionEvent(builder, reEncodedHtml);
    }
}
