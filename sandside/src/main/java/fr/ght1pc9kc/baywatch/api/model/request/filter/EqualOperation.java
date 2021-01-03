package fr.ght1pc9kc.baywatch.api.model.request.filter;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EqualOperation<T> extends BiOperand<T> {
    public EqualOperation(CriterionProperty criteria, CriterionValue<T> value) {
        super(criteria, value);
    }

    @Override
    public <R> R visit(Visitor<R> visitor) {
        return visitor.visitEqual(this);
    }
}
