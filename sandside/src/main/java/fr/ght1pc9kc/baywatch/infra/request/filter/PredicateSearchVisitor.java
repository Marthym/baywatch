package fr.ght1pc9kc.baywatch.infra.request.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ght1pc9kc.baywatch.api.model.request.filter.*;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;

public class PredicateSearchVisitor<E> implements Criteria.Visitor<Predicate<E>> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Predicate<E> visitNoCriteria(NoCriterion none) {
        return n -> true;
    }

    @Override
    public Predicate<E> visitAnd(AndOperation operation) {
        return n -> operation.andCriteria.stream()
                .map(a -> a.visit(this))
                .allMatch(a -> a.test(n));
    }

    @Override
    public Predicate<E> visitNot(NotOperation operation) {
        return n -> !operation.negative.visit(this).test(n);
    }

    @Override
    public Predicate<E> visitOr(OrOperation operation) {
        return n -> operation.orCriteria.stream()
                .map(a -> a.visit(this))
                .anyMatch(a -> a.test(n));
    }

    @Override
    public <T> Predicate<E> visitIn(InOperation<T> operation) {
        return n -> {
            Map<String, Object> json = mapper.convertValue(n, new TypeReference<>() {
            });
            Object o = json.get(operation.field.property);
            if (o == null) return true;
            else return operation.value.value.stream().anyMatch(o::equals);
        };
    }

    @Override
    public <T> Predicate<E> visitEqual(EqualOperation<T> operation) {
        return n -> {
            Map<String, Object> json = mapper.convertValue(n, new TypeReference<>() {
            });
            Object o = json.get(operation.field.property);
            if (o == null) return true;
            else return o.equals(operation.value.value);
        };
    }

    @Override
    public <T> Predicate<E> visitGreaterThan(GreaterThanOperation<T> operation) {
        return n -> {
            Map<String, Comparable<T>> json = mapper.convertValue(n, new TypeReference<>() {
            });
            Comparable<T> o = json.get(operation.field.property);
            if (o == null) return true;
            else return o.compareTo(operation.value.value) > 0;
        };
    }

    @Override
    public <T> Predicate<E> visitLowerThan(LowerThanOperation<T> operation) {
        return n -> {
            Map<String, Comparable<T>> json = mapper.convertValue(n, new TypeReference<>() {
            });
            Comparable<T> o = json.get(operation.field.property);
            if (o == null) return true;
            else return o.compareTo(operation.value.value) < 0;
        };
    }
}
