package fr.ght1pc9kc.baywatch.api.model.request;

import fr.ght1pc9kc.baywatch.api.model.request.filter.Criteria;
import fr.ght1pc9kc.baywatch.api.model.request.pagination.Sort;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder
@Getter(AccessLevel.NONE)
public class PageRequest {
    public int page;
    public int size;
    public Sort sort;
    public Criteria filter;

    public static PageRequest of(int page, int size) {
        return PageRequest.builder()
                .page(page).size(size)
                .filter(Criteria.none())
                .sort(Sort.of())
                .build();
    }
}
