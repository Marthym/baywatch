package fr.ght1pc9kc.baywatch.api.model.search;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class NotOperation extends Criteria {
    public Criteria criteria;

    @Override
    public <R> R visit(Visitor<R> visitor) {
        return visitor.visitNot(this);
    }
}
