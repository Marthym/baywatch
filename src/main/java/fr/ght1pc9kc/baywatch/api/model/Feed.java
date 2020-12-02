package fr.ght1pc9kc.baywatch.api.model;

import lombok.Builder;
import lombok.Value;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;

@Value
@Builder
public class Feed {
    public int id;
    public String name;
    public URI url;
    public Instant lastWatch;
    public Collection<Folder> folders;
}
