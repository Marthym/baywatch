package fr.ght1pc9kc.baywatch.api.model.search;

public abstract class Criteria {

    public static NoCriteria none() {
        return NoCriteria.NONE;
    }

    public static Criteria and(Criteria left, Criteria right) {
        return new AndOperation(left, right);
    }

    public static Criteria or(Criteria left, Criteria right) {
        return new OrOperation(left, right);
    }

    public static Field field(String name) {
        return new Field(name);
    }

    public Criteria and(Criteria right) {
        return and(this, right);
    }

    public Criteria or(Criteria right) {
        return or(this, right);
    }

    public abstract <R> R visit(Visitor<R> visitor);

    public interface Visitor<R> {
        R visitNoCriteria(NoCriteria none);

        R visitAnd(AndOperation operation);

        R visitOr(OrOperation operation);

        <T> R visitEqual(EqualOperation<T> operation);

        <T> R visitGreaterThan(GreaterThanOperation<T> operation);

        <T> R visitLowerThan(LowerThanOperation<T> operation);

        default <T> R visitValue(Value<T> value) {
            throw new IllegalStateException("Value not implemented in visitor");
        }
    }
}
