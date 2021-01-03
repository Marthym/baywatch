package fr.ght1pc9kc.baywatch.infra.request.filter;

import fr.ght1pc9kc.baywatch.api.model.request.filter.*;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StringSearchVisitor implements Criteria.Visitor<String> {
    @Override
    public String visitNoCriteria(NoCriterion none) {
        return "";
    }

    @Override
    public String visitAnd(AndOperation operation) {
        return operation.andCriteria.stream()
                .map(a -> a.visit(this))
                .collect(Collectors.joining(" and ", "(", ")"));
    }

    @Override
    public String visitNot(NotOperation operation) {
        return "not (" + operation.negative.visit(this) + ")";
    }

    @Override
    public String visitOr(OrOperation operation) {
        return operation.orCriteria.stream()
                .map(a -> a.visit(this))
                .collect(Collectors.joining(" or ", "(", ")"));
    }

    @Override
    public <T> String visitEqual(EqualOperation<T> operation) {
        return operation.field.property + " = '" + operation.value.visit(this) + "'";
    }

    @Override
    public <T> String visitIn(InOperation<T> operation) {
        return operation.field.property + " in " + operation.value.visit(this);
    }

    @Override
    public <T> String visitGreaterThan(GreaterThanOperation<T> operation) {
        return operation.field.property + " > " + operation.value.visit(this);
    }

    @Override
    public <T> String visitLowerThan(LowerThanOperation<T> operation) {
        return operation.field.property + " < " + operation.value.visit(this);
    }

    @Override
    public <T> String visitValue(CriterionValue<T> value) {
        return value.value.toString();
    }
}
