package fr.ght1pc9kc.baywatch.api.model.request.filter;

import lombok.*;

@Value
@Getter(AccessLevel.NONE)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
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
