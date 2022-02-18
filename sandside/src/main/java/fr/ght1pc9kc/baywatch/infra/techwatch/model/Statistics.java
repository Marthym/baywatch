package fr.ght1pc9kc.baywatch.infra.techwatch.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder
@Getter(AccessLevel.NONE)
public class Statistics {
    public int news;
    public int unread;
    public int feeds;
    public int users;
}
