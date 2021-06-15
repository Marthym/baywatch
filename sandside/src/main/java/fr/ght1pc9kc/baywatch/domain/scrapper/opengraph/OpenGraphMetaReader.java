package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph;

import com.machinezoo.noexception.Exceptions;
import com.machinezoo.noexception.throwing.ThrowingRunnable;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.Meta;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OGType;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OpenGraph;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.Tags;
import fr.ght1pc9kc.juery.basic.common.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

@Slf4j
public final class OpenGraphMetaReader {
    private static void logException(ThrowingRunnable throwable) {
        try {
            throwable.run();
        } catch (Throwable e) {
            log.warn("{}: {}", e.getClass(), e.getLocalizedMessage());
        }
    }

    public OpenGraph read(Collection<Meta> metas) {
        URI location = metas.stream()
                .filter(m -> Tags.OG_URL.equals(m.property))
                .findAny()
                .flatMap(Exceptions.silence().function(m -> URI.create(m.content)))
                .orElse(null);
        return read(metas, location);
    }

    public OpenGraph read(Collection<Meta> metas, URI location) {
        OpenGraph.OpenGraphBuilder builder = OpenGraph.builder();
        for (Meta m : metas) {
            switch (m.property) {
                case Tags.OG_TITLE:
                    builder.title(m.content);
                    break;
                case Tags.OG_TYPE:
                    logException(() ->
                            builder.type(OGType.from(m.content)));
                    break;
                case Tags.OG_URL:
                    readMetaUrl(m.content, location).ifPresent(builder::url);
                    break;
                case Tags.OG_IMAGE:
                    readMetaUri(m.content, location).ifPresent(builder::image);
                    break;
                case Tags.OG_DESCRIPTION:
                    if (!StringUtils.isBlank(m.content)) {
                        builder.description(m.content);
                    }
                    break;
                case Tags.OG_LOCALE:
                    if (m.content != null) {
                        builder.locale(Locale.forLanguageTag(m.content));
                    }
                    break;
                default:
            }
        }
        return builder.build();
    }

    private Optional<URL> readMetaUrl(String link, URI location) {
        return readMetaUri(link, location)
                .flatMap(Exceptions.log(log).function(Exceptions.wrap().function(URI::toURL)));
    }

    private Optional<URI> readMetaUri(String link, URI location) {
        if (StringUtils.isBlank(link)) {
            return Optional.empty();
        }
        Optional<URI> uri = Optional.of(link)
                .flatMap(Exceptions.silence().function(URI::create))
                .flatMap(u -> {
                    try {
                        return Optional.of(new URI(u.getScheme(), u.getHost(), u.getPath(), null));
                    } catch (URISyntaxException e) {
                        log.info("{}: {} -> {}", e.getClass(), e.getLocalizedMessage(), u);
                        log.debug("STACKTRACE", e);
                        return Optional.empty();
                    }
                });

        if (location != null) {
            return uri.map(location::resolve);
        }

        return uri;
    }
}
