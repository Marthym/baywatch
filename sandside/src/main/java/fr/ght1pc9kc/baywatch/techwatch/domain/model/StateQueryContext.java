package fr.ght1pc9kc.baywatch.techwatch.domain.model;

import lombok.Builder;

import java.util.List;
import java.util.Objects;

public record StateQueryContext(
        List<String> shared,
        List<String> read
) {
    @Builder
    public StateQueryContext(List<String> shared, List<String> read) {
        this.shared = List.copyOf(Objects.isNull(shared) ? List.of() : List.copyOf(shared));
        this.read = List.copyOf(Objects.isNull(read) ? List.of() : List.copyOf(read));
    }
}
