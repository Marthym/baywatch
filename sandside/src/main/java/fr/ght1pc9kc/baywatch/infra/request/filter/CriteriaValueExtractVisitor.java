package fr.ght1pc9kc.baywatch.infra.request.filter;

import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.filter.*;

import java.util.Optional;

public class CriteriaValueExtractVisitor<T> implements CriteriaVisitor<Optional<T>> {

    @Override
    public Optional<T> visitNoCriteria(NoCriterion none) {
        return Optional.empty();
    }

    @Override
    public Optional<T> visitAnd(AndOperation operation) {
        for (Criteria criterion : operation.andCriteria()) {
            criterion.accept(this)
        }
        return operation.andCriteria().forEach(c -> c.accept(this));
    }

    @Override
    public Optional<T> visitNot(NotOperation operation) {
        return Optional.empty();
    }

    @Override
    public Optional<T> visitOr(OrOperation operation) {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> visitEqual(EqualOperation<T> operation) {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> visitGreaterThan(GreaterThanOperation<T> operation) {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> visitLowerThan(LowerThanOperation<T> operation) {
        return Optional.empty();
    }
}
