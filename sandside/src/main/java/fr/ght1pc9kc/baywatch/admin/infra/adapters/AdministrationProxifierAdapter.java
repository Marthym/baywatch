package fr.ght1pc9kc.baywatch.admin.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.admin.domain.ports.AdministrationProxifierPort;
import fr.ght1pc9kc.baywatch.techwatch.api.ImageProxyService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.ImagePresets;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class AdministrationProxifierAdapter implements AdministrationProxifierPort {
    private final @Nullable ImageProxyService imageProxyService;

    @Override
    public Entity<RawFeed> proxifyRawFeed(Entity<RawFeed> feed) {
        if (isNull(imageProxyService)) {
            return feed;
        }
        return feed.convert(self -> self.toBuilder()
                .icon(imageProxyService.proxify(self.icon(), ImagePresets.ICON))
                .build());
    }
}
