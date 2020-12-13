package fr.ght1pc9kc.baywatch.api.model.search;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class NoCriteria extends Criteria {
    static final NoCriteria NONE = new NoCriteria();

    @Override
    public <R> R visit(Visitor<R> visitor) {
        return visitor.visitNoCriteria(this);
    }
}
