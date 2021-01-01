package fr.ght1pc9kc.baywatch.api.model.request.filter;

import java.util.Arrays;

public abstract class Criteria {

    public static NoCriterion none() {
        return NoCriterion.NONE;
    }

    public static Criteria and(Criteria left, Criteria right) {
        if (left.isEmpty()) {
            return right;
        } else if (right.isEmpty()) {
            return left;
        }
        return new AndOperation(left, right);
    }

    public static Criteria and(Criteria... left) {
        if (left.length == 0) {
            return none();
        }
        if (left.length == 1) {
            return left[0];
        }
        return and(left[0], and(Arrays.copyOfRange(left, 1, left.length)));
    }

    public static Criteria or(Criteria left, Criteria right) {
        if (left.isEmpty()) {
            return left;
        } else if (right.isEmpty()) {
            return right;
        }
        return new OrOperation(left, right);
    }

    public static Criteria not(Criteria criteria) {
        if (criteria.isEmpty()) {
            return criteria;
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
