package fr.ght1pc9kc.baywatch.infra.request.pagination;

import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import fr.ght1pc9kc.baywatch.api.model.request.pagination.Order;
import fr.ght1pc9kc.baywatch.api.model.request.pagination.Sort;
import lombok.experimental.UtilityClass;
import org.jooq.*;

import java.util.Map;

@UtilityClass
public class JooqPagination {
    public static <T extends Record> Select<T> apply(PageRequest page, Map<String, Field<?>> propertiesMapper, SelectFinalStep<T> query) {
        SelectQuery<T> result = query.getQuery();
        if (page.sort != null && !Sort.UNSORTED.equals(page.sort)) {
            for (Order order : page.sort.getOrders()) {
                Field<?> field = propertiesMapper.get(order.getProperty());
                if (field != null) {
                    SortField<?> sort = field.sort(SortOrder.valueOf(order.getDirection().name()));
                    result.addOrderBy(sort);
                }
            }
        }
        if (page.size > 0) {
            result.addLimit(page.size);
            if (page.page > 0) {
                result.addOffset(page.page * page.size);
            }
        }
        return result;
    }

    public static <T extends Record> Select<T> apply(PageRequest page, SelectFinalStep<T> query) {
        return apply(page, Map.of(), query);
    }
}
