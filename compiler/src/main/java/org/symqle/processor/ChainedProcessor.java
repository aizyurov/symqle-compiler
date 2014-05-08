package org.symqle.processor;

/**
 * Processor, which knows its predecessor.
 */
public abstract class ChainedProcessor implements Processor {
    /**
     * Processor, which should be called before {@code this}.
     * @return predecessor
     */
    protected abstract Processor predecessor();
}
