package fr.ght1pc9kc.baywatch.api.model.request.pagination;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Delegate;
import reactor.core.CorePublisher;
import reactor.core.publisher.Flux;

@Value
@AllArgsConstructor
public class Page<T> implements CorePublisher<T> {
    @Delegate(types = Flux.class)
    Flux<T> content;
    int totalElements;
    int totalPages;

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }
}
