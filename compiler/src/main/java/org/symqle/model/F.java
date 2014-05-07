package org.symqle.model;

/**
 * Function, which may throw exception.
 * @param <Arg> argunment type
 * @param <Res> result type
 * @param <Ex> thrown exception
 */
public interface F<Arg, Res, Ex extends Exception> {
    /**
     * Apply this function and return result.
     * @param arg argument
     * @return result
     * @throws Ex implementation-defined
     */
    Res apply(Arg arg) throws Ex;
}
