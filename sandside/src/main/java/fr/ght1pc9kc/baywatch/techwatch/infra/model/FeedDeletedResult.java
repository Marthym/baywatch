package fr.ght1pc9kc.baywatch.techwatch.infra.model;

import lombok.Value;

@Value
public class FeedDeletedResult {
    public int unsubscribed;
    public int purged;
}
