package fr.ght1pc9kc.baywatch.api.scrapper;

import fr.ght1pc9kc.baywatch.api.model.News;

import javax.annotation.Nonnull;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;

public interface FeedParserPlugin {
    String pluginForDomain();

    default News.NewsBuilder handleItemEvent() {
        return News.builder().id(UUID.randomUUID());
    }

    default News.NewsBuilder handleTitleEvent(@Nonnull News.NewsBuilder builder, String title) {
        return builder.title(title);
    }

    default News.NewsBuilder handleDescriptionEvent(@Nonnull News.NewsBuilder builder, String content) {
        return builder.description(content);
    }

    default News.NewsBuilder handleLinkEvent(@Nonnull News.NewsBuilder builder, URI link) {
        return builder.link(link);
    }

    default News.NewsBuilder handlePublicationEvent(@Nonnull News.NewsBuilder builder, Instant publishedAt) {
        return builder.publication(publishedAt);
    }

    default News handleEndEvent(@Nonnull News.NewsBuilder builder) {
        return builder.build();
    }
}
