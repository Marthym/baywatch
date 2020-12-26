package fr.ght1pc9kc.baywatch.api.model.request.pagination;

import lombok.Value;

@Value
public class Order {
    Direction direction;
    String property;
}
