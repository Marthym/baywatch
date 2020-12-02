package fr.ght1pc9kc.baywatch.domain.scrapper.plugins;

import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.scrapper.FeedParserPlugin;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public final class RedditParserPlugin implements FeedParserPlugin {

    @Override
    public String pluginForDomain() {
        return "www.reddit.com";
    }

    @Override
    public News.NewsBuilder handleDescriptionEvent(@Nonnull News.NewsBuilder builder, String content) {
        return builder.description("Reddit");
    }
}
