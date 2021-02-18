package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model;

import com.machinezoo.noexception.Exceptions;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Locale;

@With
@Value
@Builder
public class OpenGraph {
    public String title;
    public OGType type;
    public URL url;
    public URI image;
    public String description;
    public Locale locale;

    public static OpenGraph fromMetas(Collection<Meta> metas) {
        OpenGraphBuilder builder = OpenGraph.builder();
        for (Meta m : metas) {
            switch (m.property) {
                case Tags.OG_TITLE:
                    builder.title(m.content);
                    break;
                case Tags.OG_TYPE:
                    builder.type(OGType.valueOf(m.content.toUpperCase()));
                    break;
                case Tags.OG_URL:
                    Exceptions.silence()
                            .get(Exceptions.sneak().supplier(() -> new URL(m.content)))
                            .ifPresent(builder::url);
                    break;
                case Tags.OG_IMAGE:
                    builder.image(URI.create(m.content));
                    break;
                case Tags.OG_DESCRIPTION:
                    builder.description(m.content);
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
}
