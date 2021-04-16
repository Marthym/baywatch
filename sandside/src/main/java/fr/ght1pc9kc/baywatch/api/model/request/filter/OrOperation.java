package fr.ght1pc9kc.baywatch.api.model.request.filter;

import lombok.Value;

import java.util.List;
import java.util.Objects;

@Value
public class OrOperation extends Criteria {
    public List<Criteria> orCriteria;

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public <R> R visit(Visitor<R> visitor) {
        return visitor.visitOr(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OrOperation that = (OrOperation) o;
        return this.orCriteria.containsAll(that.orCriteria)
                && that.orCriteria.containsAll(this.orCriteria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), orCriteria);
    }
}
