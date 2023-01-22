package fr.ght1pc9kc.baywatch.techwatch.domain.model;

import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.api.Pagination;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;

@Value
@Builder
public final class QueryContext {
    @Builder.Default
    public final Pagination pagination = Pagination.ALL;
    public final Criteria filter;
    @With
    public final String userId;

    public static QueryContext from(PageRequest pr) {
        return new QueryContext(pr.pagination(), pr.filter(), null);
    }

    public static QueryContext all(Criteria filter) {
        return new QueryContext(Pagination.ALL, filter, null);
    }

    public static QueryContext first(Criteria filter) {
        return new QueryContext(Pagination.FIRST, filter, null);
    }

    public static QueryContext first(QueryContext qCtx) {
        return new QueryContext(Pagination.FIRST, qCtx.filter, qCtx.userId);
    }

    /**
     * Build a context to retrieve <b>only ONE element</b> by its ID.
     *
     * @param id The ID of the wanted element
     * @return A context for ONE and only ONE element
     */
    public static QueryContext id(String id) {
        return new QueryContext(Pagination.FIRST, Criteria.property(ID).eq(id), null);
    }

    public static QueryContext empty() {
        return new QueryContext(Pagination.ALL, Criteria.none(), null);
    }

    public boolean isScoped() {
        return userId != null && !userId.isBlank();
    }
}
