package fr.ght1pc9kc.baywatch.api.model.request.filter;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

@Value
@Getter(AccessLevel.NONE)
@EqualsAndHashCode(callSuper = true)
public class CriterionValue<T> extends Criteria {
    public T value;

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public <R> R visit(Visitor<R> visitor) {
        return visitor.visitValue(this);
    }
}
