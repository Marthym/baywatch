package fr.ght1pc9kc.baywatch.infra.request.pagination;

import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import lombok.experimental.UtilityClass;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectLimitPercentStep;

@UtilityClass
public class JooqPagination {
    public static <T extends Record> Select<T> apply(PageRequest page, SelectConditionStep<T> query) {
        Select<T> result = query;
        if (page.size > 0) {
            SelectLimitPercentStep<T> limit = query.limit(page.size);
            result = query;
            if (page.page > 0) {
                result = limit.offset(page.page * page.size);
            }
        }
        return result;
    }
}
