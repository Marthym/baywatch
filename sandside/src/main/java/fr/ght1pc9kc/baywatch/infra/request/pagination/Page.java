package fr.ght1pc9kc.baywatch.infra.request.pagination;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

public class Page<T> extends ResponseEntity<Flux<T>> {

    public Page(HttpStatus status, MultiValueMap<String, String> headers, Flux<T> body) {
        super(body, headers, status);
    }
}
