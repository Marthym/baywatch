package fr.ght1pc9kc.baywatch.api.model.search;

import lombok.EqualsAndHashCode;

@lombok.Value
@EqualsAndHashCode(callSuper = true)
public class Value<T> extends Criteria {
    public T value;

    @Override
    public <R> R visit(Visitor<R> visitor) {
        return visitor.visitValue(this);
    }
}
