package fr.ght1pc9kc.baywatch.api.model;

import lombok.Builder;
import lombok.Value;

import java.net.URL;
import java.time.Instant;

@Value
@Builder
public class News {
    int id;
    String title;
    String description;
    Instant publication;
    URL link;
    boolean stared;
}
