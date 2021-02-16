package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.net.MalformedURLException;
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
                case "og:title":
                    builder.title(m.content);
                    break;
                case "og:type":
                    builder.type(OGType.valueOf(m.content.toUpperCase()));
                    break;
                case "og:url":
                    try {
                        builder.url(new URL(m.content));
                    } catch (MalformedURLException e) {
                        throw new OpenGraphException(e);
                    }
                    break;
                case "og:image":
                    builder.image(URI.create(m.content));
                    break;
                case "og:description":
                    builder.description(m.content);
                    break;
                case "og:locale":
                    builder.locale(Locale.forLanguageTag(m.content));
                    break;
                default:
            }
        }
        return builder.build();
    }
}
