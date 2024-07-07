package fr.ght1pc9kc.baywatch.common.api.model;

import java.net.InetSocketAddress;

public record ClientInfoContext(
        InetSocketAddress ip,
        String userAgent
) {
}
