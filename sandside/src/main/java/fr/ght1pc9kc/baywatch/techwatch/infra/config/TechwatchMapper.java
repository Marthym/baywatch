package fr.ght1pc9kc.baywatch.techwatch.infra.config;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.ImageProxyProperties;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.Set;

@Mapper(componentModel = "spring", imports = {URI.class, Set.class, Hasher.class, ByteBuffer.class, HexFormat.class})
public interface TechwatchMapper {
    @Mapping(target = "raw.id",
            expression = "java(Hasher.identify(atomFeed.link()))")
    @Mapping(source = "title", target = "raw.name")
    @Mapping(source = "title", target = "name")
    @Mapping(source = "description", target = "raw.description")
    @Mapping(source = "link", target = "raw.url")
    @Mapping(target = "tags", expression = "java(Set.of())")
    Feed getFeedFromAtom(AtomFeed atomFeed);

    @Mapping(target = "signingKey",
            expression = "java(ByteBuffer.wrap(HexFormat.of().parseHex(config.signingKey())))")
    @Mapping(target = "signingSalt",
            expression = "java(ByteBuffer.wrap(HexFormat.of().parseHex(config.signingSalt())))")
    ImageProxyProperties toProperties(ImageProxyConfig config);
}
