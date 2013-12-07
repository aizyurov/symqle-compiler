package org.symqle.processor;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 07.12.2013
 * Time: 13:22:50
 * To change this template use File | Settings | File Templates.
 */
public abstract class ChainedProcessor implements Processor {
    protected abstract Processor predecessor();
}
