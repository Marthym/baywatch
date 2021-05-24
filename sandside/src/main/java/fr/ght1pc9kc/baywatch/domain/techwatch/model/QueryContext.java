package fr.ght1pc9kc.baywatch.domain.techwatch.model;

import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.api.Pagination;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@Builder
public final class QueryContext {
    public final Pagination pagination;
    public final Criteria filter;
    @With
    public final String userId;

    public static QueryContext from(PageRequest pr) {
        return new QueryContext(pr.pagination(), pr.filter(), null);
    }

    public static QueryContext filter(Criteria filter) {
        return new QueryContext(Pagination.ALL, filter, null);
    }

    public static QueryContext empty() {
        return new QueryContext(Pagination.ALL, Criteria.none(), null);
    }
}
