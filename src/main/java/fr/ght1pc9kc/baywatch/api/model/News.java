package fr.ght1pc9kc.baywatch.api.model;

import lombok.Builder;
import lombok.Value;

import java.net.URI;
import java.time.Instant;

@Value
@Builder
public class News {
    int id;
    String title;
    String description;
    Instant publication;
    URI link;
    boolean stared;
}
