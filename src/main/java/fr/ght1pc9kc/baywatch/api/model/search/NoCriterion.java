package fr.ght1pc9kc.baywatch.api.model.search;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class NoCriterion extends Criteria {
    static final NoCriterion NONE = new NoCriterion();

    @Override
    public <R> R visit(Visitor<R> visitor) {
        return visitor.visitNoCriteria(this);
    }
}
