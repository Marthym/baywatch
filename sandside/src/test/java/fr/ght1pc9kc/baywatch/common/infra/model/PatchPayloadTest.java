package fr.ght1pc9kc.baywatch.common.infra.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertAll;

class PatchPayloadTest {
    private final ObjectMapper tested = new ObjectMapper()
            .findAndRegisterModules();

    @Test
    void should_deserialize_PatchPayload() throws IOException {
        PatchPayload actual = tested.readValue("""
                    [
                      { "op": "remove", "path": "/feeds/26722e2a1658e3d3d27e9dcad9b192974b1b997eefb06948faab3bc8ed9309d8"},
                      { "op": "move", "from": "/feeds/bf65c9d2128ef29a1f9037f8aebb1f5629769834ecb517952de1d5865aff5a42", "path": "/feeds/bf65c9d2128ef29a1f9037f8aebb1f5629769834ecb517952de1d5865aff5a34"},
                      { "op": "remove", "path": "/feeds/2d74d3c8063a252a666af515cbbeb54c59983c26225c2ca187654d4da749c0a4"}
                    ]
                """, PatchPayload.class);

        Assertions.assertThat(actual).isNotNull();
        assertAll(
                () -> Assertions.assertThat(actual.getResources().get(0)).isEqualTo(new ResourcePatch(
                        PatchOperation.remove, null,
                        URI.create("/feeds/26722e2a1658e3d3d27e9dcad9b192974b1b997eefb06948faab3bc8ed9309d8"),
                        null)),
                () -> Assertions.assertThat(actual.getResources().get(1)).isEqualTo(new ResourcePatch(
                        PatchOperation.move,
                        URI.create("/feeds/bf65c9d2128ef29a1f9037f8aebb1f5629769834ecb517952de1d5865aff5a42"),
                        URI.create("/feeds/bf65c9d2128ef29a1f9037f8aebb1f5629769834ecb517952de1d5865aff5a34"),
                        null)),
                () -> Assertions.assertThat(actual.getResources().get(2)).isEqualTo(new ResourcePatch(
                        PatchOperation.remove, null,
                        URI.create("/feeds/2d74d3c8063a252a666af515cbbeb54c59983c26225c2ca187654d4da749c0a4"),
                        null))
        );
    }
}