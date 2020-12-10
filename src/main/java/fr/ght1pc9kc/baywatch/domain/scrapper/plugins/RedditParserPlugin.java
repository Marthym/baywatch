package fr.ght1pc9kc.baywatch.domain.scrapper.plugins;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.scrapper.FeedParserPlugin;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public final class RedditParserPlugin implements FeedParserPlugin {

    private static final String LINK = "link";
    private static final Pattern LINK_EXTRACT_PATTERN =
            Pattern.compile("href=\"(?<" + LINK + ">[^\"]*)\">\\[link]", Pattern.MULTILINE);

    @Override
    public String pluginForDomain() {
        return "www.reddit.com";
    }

    @Override
    public News.NewsBuilder handleDescriptionEvent(@Nonnull News.NewsBuilder builder, String content) {
        String parsableContent = HtmlUtils.htmlUnescape(content);
        Matcher m = LINK_EXTRACT_PATTERN.matcher(parsableContent);
        if (m.find()) {
            builder.link(URI.create(m.group(LINK)));
        }
        return builder.description(content);
    }

    @Override
    public News.NewsBuilder handleLinkEvent(@Nonnull News.NewsBuilder builder, URI link) {
        URI uri = Exceptions.silence().get(builder::build)
                .flatMap(n -> Optional.ofNullable(n.link))
                .orElse(link);

        return builder.id(Hasher.sha3(uri.toString())).link(uri);
    }
}
