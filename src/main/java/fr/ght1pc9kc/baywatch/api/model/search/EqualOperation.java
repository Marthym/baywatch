package fr.ght1pc9kc.baywatch.api.model.search;

import lombok.EqualsAndHashCode;

@lombok.Value
@EqualsAndHashCode(callSuper = true)
public class EqualOperation<T> extends BiOperand<T> {
    public EqualOperation(Field criteria, Value<T> value) {
        super(criteria, value);
    }

    @Override
    public <R> R visit(Visitor<R> visitor) {
        return visitor.visitEqual(this);
    }
}
