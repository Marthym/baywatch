package fr.ght1pc9kc.baywatch.domain.techwatch.filter;

import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.filter.ContainsOperation;
import fr.ght1pc9kc.juery.basic.filter.DefaultCriteriaVisitor;

import static fr.ght1pc9kc.baywatch.api.model.EntitiesProperties.TAGS_SEPARATOR;

public class CriteriaModifierVisitor extends DefaultCriteriaVisitor {
    @Override
    public <T> Criteria visitContains(ContainsOperation<T> operation) {
        return (operation.value.value instanceof String) ? Criteria.property(operation.field.property)
                .contains(TAGS_SEPARATOR + operation.value.value + TAGS_SEPARATOR) : operation;
    }
}
