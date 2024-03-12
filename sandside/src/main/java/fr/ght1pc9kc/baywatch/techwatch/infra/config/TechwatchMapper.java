package fr.ght1pc9kc.baywatch.techwatch.infra.config;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.ImageProxyProperties;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Set;

@Mapper(componentModel = "spring",
        imports = {URI.class, Set.class, Hasher.class, ByteBuffer.class, HexFormat.class, Instant.class})
public interface TechwatchMapper {
    @Mapping(source = "title", target = "name")
    @Mapping(source = "link", target = "location")
    @Mapping(target = "tags", expression = "java(Set.of())")
    WebFeed getFeedFromAtom(AtomFeed atomFeed);

    @Mapping(target = "signingKey",
            expression = "java(ByteBuffer.wrap(HexFormat.of().parseHex(config.signingKey())))")
    @Mapping(target = "signingSalt",
            expression = "java(ByteBuffer.wrap(HexFormat.of().parseHex(config.signingSalt())))")
    ImageProxyProperties toProperties(ImageProxyConfig config);
}
