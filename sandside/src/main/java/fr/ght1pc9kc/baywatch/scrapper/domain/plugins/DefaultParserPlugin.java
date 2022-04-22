package fr.ght1pc9kc.baywatch.scrapper.domain.plugins;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.scrapper.api.FeedParserPlugin;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
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
        return builder.id(Hasher.identify(link))
                .link(link);
    }
}
