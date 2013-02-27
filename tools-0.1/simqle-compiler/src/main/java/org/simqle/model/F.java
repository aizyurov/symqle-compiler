package org.simqle.model;

/**
 * Created by IntelliJ IDEA.
 * User: aizyurov
 * Date: 24.11.2012
 * Time: 21:40:34
 * To change this template use File | Settings | File Templates.
 */
public abstract class F<Arg, Res, Ex extends Exception> {
    public abstract Res apply(Arg arg) throws Ex;
}
