package fr.ght1pc9kc.baywatch.api.model.search;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class BiOperand<T> extends Criteria {
    public Field field;
    public Value<T> value;
}
