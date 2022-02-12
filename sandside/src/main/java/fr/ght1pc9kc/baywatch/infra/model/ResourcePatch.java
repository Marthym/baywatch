package fr.ght1pc9kc.baywatch.infra.model;

import com.fasterxml.jackson.databind.JsonNode;
import fr.ght1pc9kc.baywatch.infra.common.model.PatchOperation;
import lombok.Value;

import java.nio.file.Path;

@Value
public class ResourcePatch {
    public final PatchOperation op;
    public final Path from;
    public final Path path;
    public final JsonNode value;
}
