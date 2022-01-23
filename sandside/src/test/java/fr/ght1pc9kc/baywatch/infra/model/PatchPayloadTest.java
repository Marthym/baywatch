package fr.ght1pc9kc.baywatch.infra.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertAll;

class PatchPayloadTest {
    private final ObjectMapper tested = new ObjectMapper()
            .findAndRegisterModules();

    @Test
    void should_deserialize_PatchPayload() throws IOException {
        PatchPayload actual = tested.readValue("[\n" +
                "  { \"op\": \"remove\", \"path\": \"/feeds/26722e2a1658e3d3d27e9dcad9b192974b1b997eefb06948faab3bc8ed9309d8\"},\n" +
                "  { \"op\": \"move\", \"from\": \"/feeds/bf65c9d2128ef29a1f9037f8aebb1f5629769834ecb517952de1d5865aff5a42\", " +
                "       \"path\": \"/feeds/bf65c9d2128ef29a1f9037f8aebb1f5629769834ecb517952de1d5865aff5a34\"},\n" +
                "  { \"op\": \"remove\", \"path\": \"/feeds/2d74d3c8063a252a666af515cbbeb54c59983c26225c2ca187654d4da749c0a4\"}\n" +
                "]", PatchPayload.class);

        Assertions.assertThat(actual).isNotNull();
        assertAll(
                () -> Assertions.assertThat(actual.getOperations().get(0)).isEqualTo(new ResourcePatch(
                        PatchOperation.remove, null,
                        Paths.get("/feeds/26722e2a1658e3d3d27e9dcad9b192974b1b997eefb06948faab3bc8ed9309d8"),
                        null)),
                () -> Assertions.assertThat(actual.getOperations().get(1)).isEqualTo(new ResourcePatch(
                        PatchOperation.move,
                        Paths.get("/feeds/bf65c9d2128ef29a1f9037f8aebb1f5629769834ecb517952de1d5865aff5a42"),
                        Paths.get("/feeds/bf65c9d2128ef29a1f9037f8aebb1f5629769834ecb517952de1d5865aff5a34"),
                        null)),
                () -> Assertions.assertThat(actual.getOperations().get(2)).isEqualTo(new ResourcePatch(
                        PatchOperation.remove, null,
                        Paths.get("/feeds/2d74d3c8063a252a666af515cbbeb54c59983c26225c2ca187654d4da749c0a4"),
                        null))
        );
    }
}