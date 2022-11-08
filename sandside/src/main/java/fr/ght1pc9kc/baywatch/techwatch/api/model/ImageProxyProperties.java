package fr.ght1pc9kc.baywatch.techwatch.api.model;

import java.nio.ByteBuffer;

public record ImageProxyProperties(
        ByteBuffer signingKey,
        ByteBuffer signingSalt,
        String pathBase,
        String processing,
        String extension
) {
}
