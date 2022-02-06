package fr.ght1pc9kc.baywatch.infra.common.model;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

public final class Page<T> extends ResponseEntity<Flux<T>> {
    Page(MultiValueMap<String, String> headers, Flux<T> body) {
        super(body, headers, HttpStatus.OK);
    }

    public static <T> Page<T> of(Flux<T> body, int totalCount) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-Total-Count", Integer.toString(totalCount));
        return new Page<T>(httpHeaders, body);
    }
}
