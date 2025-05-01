package fr.ght1pc9kc.baywatch.admin.infra.mappers;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.admin.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.admin.infra.model.AdminRawFeedForm;
import fr.ght1pc9kc.baywatch.common.api.model.FeedMeta;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.entity.api.Entity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RawFeedMapper {
    default Entity<RawFeed> toRawFeed(Entity<WebFeed> webFeed) {
        return Entity.identify(toRawFeed(webFeed.self()).toBuilder()
                        .lastETag(webFeed.meta(FeedMeta.ETag).orElse(null))
                        .lastWatch(webFeed.meta(FeedMeta.updated, Instant.class).orElse(null))
                        .build())
                .withId(webFeed.id());
    }

    @Mapping(target = "url", source = "location")
    @Mapping(target = "lastWatch", ignore = true)
    @Mapping(target = "lastETag", ignore = true)
    RawFeed toRawFeed(WebFeed webFeed);

    @InheritInverseConfiguration
    WebFeed toWebFeed(RawFeed rawFeed);

    default Entity<WebFeed> toWebFeed(Entity<RawFeed> rawFeed) {
        return Entity.identify(toWebFeed(rawFeed.self()))
                .meta(FeedMeta.ETag, rawFeed.self().lastETag())
                .meta(FeedMeta.updated, rawFeed.self().lastWatch())
                .withId(rawFeed.id());
    }

    RawFeed toRawFeed(AdminRawFeedForm form);

    default Map<String, Object> convertValue(Entity<RawFeed> rawFeedEntity) {
        Map<String, Object> map = new HashMap<>();

        map.put("_id", rawFeedEntity.id());

        RawFeed pojo = rawFeedEntity.self();
        Field[] fields = pojo.getClass().getDeclaredFields();

        for (Field field : fields) {
            boolean canAccess = field.canAccess(pojo);
            Exceptions.silence().run(Exceptions.sneak().runnable(() -> {
                if (!canAccess) {
                    field.setAccessible(true);
                }

                Object value = field.get(pojo);
                if (nonNull(value)) {
                    map.put(field.getName(), value);
                }
            }));
            if (!canAccess) {
                field.setAccessible(false);
            }
        }

        return Map.copyOf(map);
    }
}
