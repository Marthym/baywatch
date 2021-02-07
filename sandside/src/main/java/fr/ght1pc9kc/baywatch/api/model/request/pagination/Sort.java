package fr.ght1pc9kc.baywatch.api.model.request.pagination;

import lombok.Value;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Value
public class Sort {
    public static final Sort UNSORTED = new Sort(List.of());

    List<Order> orders;

    public static Sort of() {
        return UNSORTED;
    }

    public static Sort of(Direction direction, String... properties) {
        return new Sort(Arrays.stream(properties)
                .map(p -> new Order(direction, p))
                .collect(Collectors.toUnmodifiableList()));
    }

    public static Sort of(Order... orders) {
        return new Sort(List.of(orders));
    }
}
