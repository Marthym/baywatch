package fr.ght1pc9kc.baywatch.infra.search;

import fr.ght1pc9kc.baywatch.api.model.search.*;
import fr.ght1pc9kc.baywatch.infra.mappers.NewsToRecordConverter;
import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.jooq.impl.DSL;

@AllArgsConstructor
public class JooqSearchVisitor implements Criteria.Visitor<Condition> {

    @Override
    public Condition visitNoCriteria(NoCriteria none) {
        return DSL.noCondition();
    }

    @Override
    public Condition visitAnd(AndOperation operation) {
        Condition right = operation.right.visit(this);
        Condition left = operation.left.visit(this);
        return DSL.and(right, left);
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

    @Override
    public <T> Condition visitValue(Value<T> value) {
        throw new IllegalArgumentException("No need of value visitor");
    }

    @SuppressWarnings("unchecked")
    private <T> org.jooq.Field<T> readField(Field field) {
        return (org.jooq.Field<T>) NewsToRecordConverter.PROPERTIES_MAPPING
                .getOrDefault(field.name, DSL.field(field.name));
    }
}
