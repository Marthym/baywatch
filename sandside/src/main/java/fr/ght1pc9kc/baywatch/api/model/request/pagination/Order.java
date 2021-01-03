package fr.ght1pc9kc.baywatch.api.model.request.pagination;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class Order {
    Direction direction;
    String property;

    public Order(@NotNull Direction direction, @NotNull String property) {
        if (property.isBlank()) {
            throw new IllegalArgumentException("The property must not be blank !");
        }
        this.direction = direction;
        this.property = property;
    }

    public static Order asc(@NotNull String prop) {
        return new Order(Direction.ASC, prop);
    }

    public static Order desc(@NotNull String prop) {
        return new Order(Direction.DESC, prop);
    }
}
