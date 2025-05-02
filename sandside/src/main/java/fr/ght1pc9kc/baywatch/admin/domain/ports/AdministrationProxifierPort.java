package fr.ght1pc9kc.baywatch.admin.domain.ports;

import fr.ght1pc9kc.baywatch.admin.api.model.RawFeed;
import fr.ght1pc9kc.entity.api.Entity;

public interface AdministrationProxifierPort {
    Entity<RawFeed> proxifyRawFeed(Entity<RawFeed> feed);
}
