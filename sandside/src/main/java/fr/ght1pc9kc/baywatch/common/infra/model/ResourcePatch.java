package fr.ght1pc9kc.baywatch.common.infra.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;

public record ResourcePatch(
        PatchOperation op,
        URI from,
        URI path,
        JsonNode value
) {
}
