package fr.ght1pc9kc.baywatch.infra.search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.search.*;

import java.util.Map;
import java.util.function.Predicate;

public class PredicateSearchVisitor implements Criteria.Visitor<Predicate<News>> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Predicate<News> visitNoCriteria(NoCriterion none) {
        return n -> true;
    }

    @Override
    public Predicate<News> visitAnd(AndOperation operation) {
        return n -> operation.right.visit(this).test(n)
                && operation.left.visit(this).test(n);
    }

    @Override
    public Predicate<News> visitNot(NotOperation operation) {
        return n -> !operation.criteria.visit(this).test(n);
    }

    @Override
    public Predicate<News> visitOr(OrOperation operation) {
        return n -> operation.right.visit(this).test(n)
                || operation.left.visit(this).test(n);
    }

    @Override
    public <T> Predicate<News> visitIn(InOperation<T> operation) {
        return n -> {
            Map<String, Object> json = mapper.convertValue(n, new TypeReference<>() {
            });
            Object o = json.get(operation.field.property);
            if (o == null) return true;
            else return operation.value.value.stream().anyMatch(o::equals);
        };
    }

    @Override
    public <T> Predicate<News> visitEqual(EqualOperation<T> operation) {
        return n -> {
            Map<String, Object> json = mapper.convertValue(n, new TypeReference<>() {
            });
            Object o = json.get(operation.field.property);
            if (o == null) return true;
            else return o.equals(operation.value.value);
        };
    }

    @Override
    public <T> Predicate<News> visitGreaterThan(GreaterThanOperation<T> operation) {
        return n -> {
            Map<String, Comparable<T>> json = mapper.convertValue(n, new TypeReference<>() {
            });
            Comparable<T> o = json.get(operation.field.property);
            if (o == null) return true;
            else return o.compareTo(operation.value.value) > 0;
        };
    }

    @Override
    public <T> Predicate<News> visitLowerThan(LowerThanOperation<T> operation) {
        return n -> {
            Map<String, Comparable<T>> json = mapper.convertValue(n, new TypeReference<>() {
            });
            Comparable<T> o = json.get(operation.field.property);
            if (o == null) return true;
            else return o.compareTo(operation.value.value) < 0;
        };
    }
}
