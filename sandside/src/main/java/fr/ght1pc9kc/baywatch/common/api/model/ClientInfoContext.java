package fr.ght1pc9kc.baywatch.common.api.model;

import java.net.InetSocketAddress;
import java.net.URI;

public record ClientInfoContext(
        InetSocketAddress ip,
        String userAgent,
        URI baseUrl
) {
}
