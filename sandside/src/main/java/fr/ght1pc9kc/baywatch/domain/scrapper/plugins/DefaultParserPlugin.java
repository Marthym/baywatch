package fr.ght1pc9kc.baywatch.domain.scrapper.plugins;

import fr.ght1pc9kc.baywatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.api.scrapper.FeedParserPlugin;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.net.URI;

@Component
public final class DefaultParserPlugin implements FeedParserPlugin {
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
        return builder.id(Hasher.sha3(link.toString()))
                .link(link);
    }
}
