package fr.ght1pc9kc.baywatch.api.model.search;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class AndOperation extends Criteria {
    public Criteria left;
    public Criteria right;

    @Override
    public <R> R visit(Visitor<R> visitor) {
        return visitor.visitAnd(this);
    }
}
