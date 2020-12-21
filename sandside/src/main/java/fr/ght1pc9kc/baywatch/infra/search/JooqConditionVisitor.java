package fr.ght1pc9kc.baywatch.infra.search;

import fr.ght1pc9kc.baywatch.api.model.search.*;
import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@AllArgsConstructor
public class JooqConditionVisitor implements Criteria.Visitor<Condition> {

    private final Function<String, Field<?>> propertiesSupplier;

    @Override
    public Condition visitNoCriteria(NoCriterion none) {
        return DSL.noCondition();
    }

    @Override
    public Condition visitAnd(AndOperation operation) {
        Condition right = operation.right.visit(this);
        Condition left = operation.left.visit(this);
        return DSL.and(right, left);
    }

    @Override
    public Condition visitNot(NotOperation operation) {
        return DSL.not(operation.criteria.visit(this));
    }

    @Override
    public Condition visitOr(OrOperation operation) {
        Condition right = operation.right.visit(this);
        Condition left = operation.left.visit(this);
        return DSL.or(right, left);
    }

    @Override
    public <T> Condition visitIn(InOperation<T> operation) {
        return readFieldToCondition(operation, Field::in);
    }

    @Override
    public <T> Condition visitEqual(EqualOperation<T> operation) {
        return readFieldToCondition(operation, Field::eq);
    }

    @Override
    public <T> Condition visitGreaterThan(GreaterThanOperation<T> operation) {
        return readFieldToCondition(operation, Field::gt);
    }

    @Override
    public <T> Condition visitLowerThan(LowerThanOperation<T> operation) {
        return readFieldToCondition(operation, Field::lt);
    }

    @SuppressWarnings("unchecked")
    private <T> Condition readFieldToCondition(BiOperand<T> operation, BiFunction<Field<T>, T, Condition> op) {
        return Optional.ofNullable(propertiesSupplier.apply(operation.field.property))
                .map(f -> op.apply((Field<T>) f, operation.value.value))
                .orElse(DSL.noCondition());
    }
}
