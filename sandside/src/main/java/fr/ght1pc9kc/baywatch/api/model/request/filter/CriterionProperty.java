package fr.ght1pc9kc.baywatch.api.model.request.filter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Value
@Getter(AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class CriterionProperty {
    public String property;

    public <T> Criteria eq(T value) {
        if (value == null) {
            return isNull();
        }
        return new EqualOperation<>(this, new CriterionValue<>(value));
    }

    public <T> Criteria in(Collection<T> values) {
        return new InOperation<>(this, new CriterionValue<>(values));
    }

    @SafeVarargs
    public final <T> Criteria in(@NotNull T... values) {
        Objects.requireNonNull(values, "Values can not be null !");
        return new InOperation<>(this, new CriterionValue<>(List.of(values)));
    }

    public Criteria isNull() {
        return new EqualOperation<>(this, CriterionValue.NULL);
    }

    public <T> Criteria gt(T value) {
        return new GreaterThanOperation<>(this, new CriterionValue<>(value));
    }

    public <T> Criteria lt(T value) {
        return new LowerThanOperation<>(this, new CriterionValue<>(value));
    }
}
