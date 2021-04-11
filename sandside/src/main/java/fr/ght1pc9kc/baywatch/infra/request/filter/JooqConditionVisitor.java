package fr.ght1pc9kc.baywatch.infra.request.filter;

import fr.ght1pc9kc.baywatch.api.model.request.filter.*;
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
        Condition[] conditions = operation.andCriteria.stream()
                .map(a -> a.visit(this))
                .toArray(Condition[]::new);
        return DSL.and(conditions);
    }

    @Override
    public Condition visitNot(NotOperation operation) {
        return DSL.not(operation.negative.visit(this));
    }

    @Override
    public Condition visitOr(OrOperation operation) {
        Condition[] conditions = operation.orCriteria.stream()
                .map(o -> o.visit(this))
                .toArray(Condition[]::new);
        return DSL.or(conditions);
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
                .map(f -> {
                    if (operation.value.isNull()) {
                        return f.isNull();
                    } else {
                        return op.apply((Field<T>) f, operation.value.value);
                    }
                })
                .orElse(DSL.noCondition());
    }
}
