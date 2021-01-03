package fr.ght1pc9kc.baywatch.infra.request.filter;

import fr.ght1pc9kc.baywatch.api.model.request.filter.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class QueryStringFilterVisitor implements Criteria.Visitor<String> {
    @Override
    public String visitNoCriteria(NoCriterion none) {
        return "";
    }

    @Override
    public String visitAnd(AndOperation operation) {
        return operation.andCriteria.stream()
                .map(a -> a.visit(this))
                .collect(Collectors.joining("&"));
    }

    @Override
    public String visitNot(NotOperation operation) {
        throw new IllegalStateException("Operation 'not' not permitted in query string !");
    }

    @Override
    public String visitOr(OrOperation operation) {
        throw new IllegalStateException("Operation 'or' not permitted in query string !");
    }

    @Override
    public <T> String visitEqual(EqualOperation<T> operation) {
        return URLEncoder.encode(operation.field.property, StandardCharsets.UTF_8) + "=" + operation.value.visit(this);
    }

    @Override
    public <T> String visitIn(InOperation<T> operation) {
        throw new IllegalStateException("Operation 'in' not permitted in query string !");
    }

    @Override
    public <T> String visitGreaterThan(GreaterThanOperation<T> operation) {
        throw new IllegalStateException("Operation '>' not permitted in query string !");
    }

    @Override
    public <T> String visitLowerThan(LowerThanOperation<T> operation) {
        throw new IllegalStateException("Operation '<' not permitted in query string !");
    }

    @Override
    public <T> String visitValue(CriterionValue<T> value) {
        return URLEncoder.encode(value.value.toString(), StandardCharsets.UTF_8);
    }
}
