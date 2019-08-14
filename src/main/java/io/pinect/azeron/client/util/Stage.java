package io.pinect.azeron.client.util;

public interface Stage<I, O> {
    /**
     * @param i Input refrence
     * @param o Output refrence
     * @return boolean: determines if next stage should run
     */
    boolean process(I i, O o);
}
