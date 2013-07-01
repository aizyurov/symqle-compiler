package org.symqle.util;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 23.08.12
 * Time: 16:09
 * To change this template use File | Settings | File Templates.
 */
public class CallbackIterator<T, E extends Exception> {
    private final Iterable<T> iterable;

    public CallbackIterator(final Iterable<T> iterable) {
        this.iterable = iterable;
    }

    public void iterate(Callback<T, E> callback) throws E {
        for (Iterator<T> iterator = iterable.iterator(); iterator.hasNext(); ) {
            final T next = iterator.next();
            try {
                callback.call(next);
            } catch (StopException e) {
                break;
            }
        }
    }
}
