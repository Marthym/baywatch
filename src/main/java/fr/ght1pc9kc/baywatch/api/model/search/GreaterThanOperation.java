package fr.ght1pc9kc.baywatch.api.model.search;

import lombok.EqualsAndHashCode;

@lombok.Value
@EqualsAndHashCode(callSuper = true)
public class GreaterThanOperation<T> extends BiOperand<T> {
    public GreaterThanOperation(Field criteria, Value<T> value) {
        super(criteria, value);
    }

    @Override
    public <R> R visit(Visitor<R> visitor) {
        return visitor.visitGreaterThan(this);
    }
}
