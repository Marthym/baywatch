package fr.ght1pc9kc.baywatch.api.model.request.filter;

import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EqualsAndHashCode
public abstract class Criteria {

    public static NoCriterion none() {
        return NoCriterion.NONE;
    }

    public static Criteria and(Criteria... andCriteria) {
        List<Criteria> filtered = Arrays.stream(andCriteria)
                .filter(Predicate.not(Criteria::isEmpty))
                .flatMap(a -> {
                    if (a instanceof AndOperation) {
                        return ((AndOperation) a).andCriteria.stream();
                    } else {
                        return Stream.of(a);
                    }
                })
                .distinct()
                .collect(Collectors.toUnmodifiableList());
        if (filtered.isEmpty()) {
            return Criteria.none();
        } else if (filtered.size() == 1) {
            return filtered.iterator().next();
        }
        return new AndOperation(filtered);
    }

    public static Criteria or(Criteria... orCriteria) {
        List<Criteria> filtered = Arrays.stream(orCriteria)
                .filter(Predicate.not(Criteria::isEmpty))
                .flatMap(a -> {
                    if (a instanceof OrOperation) {
                        return ((OrOperation) a).orCriteria.stream();
                    } else {
                        return Stream.of(a);
                    }
                })
                .distinct()
                .collect(Collectors.toUnmodifiableList());
        if (filtered.isEmpty()) {
            return Criteria.none();
        } else if (filtered.size() == 1) {
            return filtered.iterator().next();
        }
        return new OrOperation(filtered);
    }

    public static Criteria not(Criteria criteria) {
        if (criteria.isEmpty()) {
            return criteria;
        }
        if (criteria instanceof NotOperation) {
            return ((NotOperation) criteria).negative;
        }
        return new NotOperation(criteria);
    }

    public static CriterionProperty property(String property) {
        return new CriterionProperty(property);
    }

    public Criteria and(Criteria right) {
        return and(this, right);
    }

    public Criteria or(Criteria right) {
        return or(this, right);
    }

    public abstract boolean isEmpty();

    public abstract <R> R visit(Visitor<R> visitor);

    public interface Visitor<R> {
        R visitNoCriteria(NoCriterion none);

        R visitAnd(AndOperation operation);

        R visitNot(NotOperation operation);

        R visitOr(OrOperation operation);

        <T> R visitEqual(EqualOperation<T> operation);

        <T> R visitGreaterThan(GreaterThanOperation<T> operation);

        <T> R visitLowerThan(LowerThanOperation<T> operation);

        default <T> R visitIn(InOperation<T> operation) {
            throw new IllegalStateException("IN operation not implemented in visitor");
        }

        default <T> R visitValue(CriterionValue<T> value) {
            throw new IllegalStateException("Value not implemented in visitor");
        }
    }
}
