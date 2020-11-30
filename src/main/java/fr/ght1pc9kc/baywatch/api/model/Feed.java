package fr.ght1pc9kc.baywatch.api.model;

import lombok.Builder;
import lombok.Value;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;

@Value
@Builder
public class Feed {
    int id;
    String name;
    URI url;
    Instant lastWatch;
    Collection<Folder> folders;
}
