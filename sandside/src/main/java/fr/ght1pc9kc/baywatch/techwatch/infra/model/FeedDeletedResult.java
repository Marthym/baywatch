package fr.ght1pc9kc.baywatch.techwatch.infra.model;

public record FeedDeletedResult(
        int unsubscribed,
        int purged
) {
}
