package fr.ght1pc9kc.baywatch.scrapper.api;

import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;

import javax.annotation.Nonnull;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;

public interface FeedParserPlugin {
    FeedParserPlugin NO_PLUGIN = () -> "*";

    String pluginForDomain();

    default RawNews.RawNewsBuilder handleItemEvent() {
        return RawNews.builder().id(UUID.randomUUID().toString());
    }

    default RawNews.RawNewsBuilder handleTitleEvent(@Nonnull RawNews.RawNewsBuilder builder, String title) {
        return builder.title(title);
    }

    default RawNews.RawNewsBuilder handleDescriptionEvent(@Nonnull RawNews.RawNewsBuilder builder, String content) {
        return builder.description(content);
    }

    default RawNews.RawNewsBuilder handleLinkEvent(@Nonnull RawNews.RawNewsBuilder builder, URI link) {
        return builder.link(link);
    }

    default RawNews.RawNewsBuilder handlePublicationEvent(@Nonnull RawNews.RawNewsBuilder builder, Instant publishedAt) {
        return builder.publication(publishedAt);
    }

    default RawNews handleEndEvent(@Nonnull RawNews.RawNewsBuilder builder) {
        return builder.build();
    }
}
