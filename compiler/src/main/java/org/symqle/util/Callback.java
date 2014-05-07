package org.symqle.util;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 23.08.12
 * Time: 16:08
 * To change this template use File | Settings | File Templates.
 */
public interface Callback<T, E extends Exception> {

    void call(T arg) throws StopException, E;
}
