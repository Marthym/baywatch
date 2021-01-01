package fr.ght1pc9kc.baywatch.infra.request;

import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import fr.ght1pc9kc.baywatch.api.model.request.filter.Criteria;
import fr.ght1pc9kc.baywatch.api.model.request.pagination.Sort;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@UtilityClass
public class PageRequestFormatter {
    private static final String DEFAULT_PAGE_PARAMETER = "_p";
    private static final String DEFAULT_SIZE_PARAMETER = "_pp";
    private static final String DEFAULT_SORT_PARAMETER = "_s";
    private static final Set<String> EXCLUDE_FILTER_PARAMETERS = Set.of(
            DEFAULT_PAGE_PARAMETER, DEFAULT_SIZE_PARAMETER, DEFAULT_SORT_PARAMETER
    );
    private static final int MAX_PAGE_SIZE = 100;

    public static PageRequest fromQueryString(Map<String, String> queryString) {
        int page = Optional.ofNullable(queryString.get(DEFAULT_PAGE_PARAMETER))
                .map(Integer::parseInt)
                .orElse(0);
        int perPage = Optional.ofNullable(queryString.get(DEFAULT_SIZE_PARAMETER))
                .map(Integer::parseInt)
                .orElse(MAX_PAGE_SIZE);
        Sort sort = Optional.ofNullable(queryString.get(DEFAULT_SORT_PARAMETER))
                .map(PageRequestFormatter::parserSortParameter)
                .orElse(Sort.of());

        Criteria[] filters = queryString.entrySet().stream()
                .filter(e -> !EXCLUDE_FILTER_PARAMETERS.contains(e.getKey()))
                .map(e -> Criteria.property(e.getKey()).eq(e.getValue()))
                .toArray(Criteria[]::new);

        return PageRequest.builder()
                .page(page)
                .size(perPage)
                .sort(sort)
                .filter(Criteria.and(filters))
                .build();
    }

    public static Sort parserSortParameter(String value) {
        return Sort.of();
    }

    public static Criteria parserFilterParameter(String value) {
        return Criteria.none();
    }
}
