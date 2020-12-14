package fr.ght1pc9kc.baywatch.infra.search;

import fr.ght1pc9kc.baywatch.api.model.search.*;

public class StringSearchVisitor implements Criteria.Visitor<String> {
    @Override
    public String visitNoCriteria(NoCriterion none) {
        return "";
    }

    @Override
    public String visitAnd(AndOperation operation) {
        return "(" + operation.left.visit(this) + " and " + operation.right.visit(this) + ")";
    }

    @Override
    public String visitOr(OrOperation operation) {
        return "(" + operation.left.visit(this) + " or " + operation.right.visit(this) + ")";
    }

    @Override
    public <T> String visitEqual(EqualOperation<T> operation) {
        return operation.field.property + " = " + operation.value.visit(this);
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
        return "\"" + value.value.toString() + "\"";
    }
}
