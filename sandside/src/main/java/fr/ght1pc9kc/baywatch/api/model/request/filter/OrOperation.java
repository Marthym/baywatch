package fr.ght1pc9kc.baywatch.api.model.request.filter;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class OrOperation extends Criteria {
    public Criteria left;
    public Criteria right;

    @Override
    public <R> R visit(Visitor<R> visitor) {
        return visitor.visitOr(this);
    }
}
