package fr.ght1pc9kc.baywatch.api.model;

import lombok.Value;

import java.net.URL;
import java.time.Instant;
import java.util.Collection;

@Value
public class Feed {
    int id;
    String name;
    URL url;
    Instant lastWatch;
    Collection<Folder> tags;
}
