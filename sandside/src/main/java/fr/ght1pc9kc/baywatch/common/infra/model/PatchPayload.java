package fr.ght1pc9kc.baywatch.common.infra.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

/**
 * Implement RFC 6902 for Patch Payload
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc5789">PATCH, RFC 5789</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc6902">JSON Patch, RFC 6902</a>
 */
@Value
@RequiredArgsConstructor(onConstructor = @__({@JsonCreator}))
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_VALUES)
public class PatchPayload {
    @JsonIgnore
    private final List<ResourcePatch> operations;
}
