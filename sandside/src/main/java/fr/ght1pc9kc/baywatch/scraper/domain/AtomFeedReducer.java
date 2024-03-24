package fr.ght1pc9kc.baywatch.scraper.domain;

import fr.ght1pc9kc.baywatch.common.api.model.FeedMeta;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.entity.api.impl.BasicEntity;
import fr.ght1pc9kc.entity.api.impl.ExtendedEntity;
import lombok.experimental.UtilityClass;

import java.util.EnumMap;

@UtilityClass
public class AtomFeedReducer {
    public Entity<AtomFeed> reduce(Entity<AtomFeed> left, Entity<AtomFeed> right) {
        EnumMap<FeedMeta, Object> metas = new EnumMap<>(FeedMeta.class);
        for (FeedMeta metaName : FeedMeta.values()) {
            left.meta(metaName, metaName.type()).ifPresent(value -> metas.put(metaName, value));
            right.meta(metaName, metaName.type()).ifPresent(value -> metas.put(metaName, value));
        }

        AtomFeed.AtomFeedBuilder atomFeedBuilder = left.self().toBuilder();
        AtomFeed rightSelf = right.self();
        if (rightSelf.id() != null) {
            atomFeedBuilder.id(rightSelf.id());
        }
        if (rightSelf.title() != null) {
            atomFeedBuilder.title(rightSelf.title());
        }
        if (rightSelf.description() != null) {
            atomFeedBuilder.description(rightSelf.description());
        }
        if (rightSelf.link() != null) {
            atomFeedBuilder.link(rightSelf.link());
        }
        if (rightSelf.updated() != null) {
            atomFeedBuilder.updated(rightSelf.updated());
        }
        if (rightSelf.author() != null) {
            atomFeedBuilder.author(rightSelf.author());
        }

        if (metas.isEmpty()) {
            return new BasicEntity<>(left.id(), atomFeedBuilder.build());
        } else {
            return new ExtendedEntity<>(left.id(), metas, atomFeedBuilder.build());
        }
    }
}
