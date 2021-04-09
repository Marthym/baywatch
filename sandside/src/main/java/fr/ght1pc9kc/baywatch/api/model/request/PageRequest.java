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
    private static final PageRequest ALL = new PageRequest(-1, -1, Sort.of(), Criteria.none());

    public int page;
    public int size;
    public Sort sort;
    public Criteria filter;

    public static PageRequest of(int page, int size) {
        if (page < 0 || size < 0) {
            return ALL;
        }
        return PageRequest.builder()
                .page(page).size(size)
                .filter(Criteria.none())
                .sort(Sort.of())
                .build();
    }

    public static PageRequest all() {
        return ALL;
    }

    public static PageRequest all(Criteria criteria) {
        return new PageRequest(-1, -1, Sort.of(), criteria);
    }

    public static PageRequest one(Criteria criteria) {
        return new PageRequest(0, 1, Sort.of(), criteria);
    }

    public PageRequest and(Criteria criteria) {
        return new PageRequest(page, size, sort, filter.and(criteria));
    }
}
