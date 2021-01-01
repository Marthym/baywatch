package fr.ght1pc9kc.baywatch.api.model.request.filter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class BiOperand<T> extends Criteria {
    public CriterionProperty field;
    public CriterionValue<T> value;

    @Override
    public boolean isEmpty() {
        return false;
    }
}
