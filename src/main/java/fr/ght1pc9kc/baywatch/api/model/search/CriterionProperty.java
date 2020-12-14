package fr.ght1pc9kc.baywatch.api.model.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
@Getter(AccessLevel.PACKAGE)
public class CriterionProperty {
    public String property;

    public <T> Criteria eq(T value) {
        return new EqualOperation<>(this, new CriterionValue<>(value));
    }

    public <T> Criteria gt(T value) {
        return new GreaterThanOperation<>(this, new CriterionValue<>(value));
    }

    public <T> Criteria lt(T value) {
        return new LowerThanOperation<>(this, new CriterionValue<>(value));
    }
}
