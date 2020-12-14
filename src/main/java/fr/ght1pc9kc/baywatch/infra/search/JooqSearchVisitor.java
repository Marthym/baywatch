package fr.ght1pc9kc.baywatch.infra.search;

import fr.ght1pc9kc.baywatch.api.model.search.*;
import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.Optional;
import java.util.function.Function;

@AllArgsConstructor
public class JooqSearchVisitor implements Criteria.Visitor<Condition> {

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
    public <T> Condition visitEqual(EqualOperation<T> operation) {
        return readField(operation.field).eq(operation.value.value);
    }

    @Override
    public <T> Condition visitGreaterThan(GreaterThanOperation<T> operation) {
        return readField(operation.field).gt(operation.value.value);
    }

    @Override
    public <T> Condition visitLowerThan(LowerThanOperation<T> operation) {
        return readField(operation.field).lt(operation.value.value);
    }

    @SuppressWarnings("unchecked")
    private <T> Field<T> readField(CriterionProperty field) {
        return Optional.ofNullable(propertiesSupplier.apply(field.property))
                .map(f -> (Field<T>) f)
                .orElseGet(() -> (Field<T>) DSL.field(field.property));
    }
}
