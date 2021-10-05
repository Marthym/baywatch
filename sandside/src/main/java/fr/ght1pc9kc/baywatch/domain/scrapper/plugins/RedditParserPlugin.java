package fr.ght1pc9kc.baywatch.domain.scrapper.plugins;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.api.scrapper.FeedParserPlugin;
import fr.ght1pc9kc.baywatch.api.scrapper.FeedScrapperPlugin;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public final class RedditParserPlugin implements FeedParserPlugin, FeedScrapperPlugin {

    private static final String LINK = "link";
    private static final Pattern LINK_EXTRACT_PATTERN =
            Pattern.compile("href=\"(?<" + LINK + ">[^\"]*)\">\\[link]", Pattern.MULTILINE);

    @Override
    public String pluginForDomain() {
        return "www.reddit.com";
    }

    @Override
    public RawNews.RawNewsBuilder handleDescriptionEvent(@Nonnull RawNews.RawNewsBuilder builder, String content) {
        String parsableContent = HtmlUtils.htmlUnescape(content);
        Matcher m = LINK_EXTRACT_PATTERN.matcher(parsableContent);
        if (m.find()) {
            builder.link(URI.create(m.group(LINK)));
        }
        return builder.description(content);
    }

    @Override
    public RawNews.RawNewsBuilder handleLinkEvent(@Nonnull RawNews.RawNewsBuilder builder, URI link) {
        URI uri = Exceptions.silence().get(builder::build)
                .map(n -> n.link)
                .orElse(link);

        return builder.id(Hasher.identify(uri)).link(uri);
    }

    @Override
    public URI uriModifier(URI original) {
        if (original.getQuery() == null || original.getQuery().isBlank()) {
            return URI.create(original + "?sort=new");
        } else {
            return URI.create(original + "&sort=new");
        }
    }
}
