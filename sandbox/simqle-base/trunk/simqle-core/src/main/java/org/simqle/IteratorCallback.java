/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle;

import java.sql.SQLException;

/**
 * <br/>07.12.2010
 *
 * @author Alexander Izyurov
 */
public interface IteratorCallback<Arg> {

    /**
     * Called by iterating object in a loop. The iterating object passes values of arg one by one.
     * @param arg next value of iterator
     * @return the steps to advance in the iterator. 1 means one forward;
     * -1 means one back (if supported by the iterator). 0 will not advance the iterator pointer and will
     * call iterate() again with the same argument - avoid infinite loop!
     * If the iterator reaches its end, it stops iterations. So, Integer.MAXINT break iterations.
     * An attempt to move before iterator start is the same as move to the start,
     * Integer.MININT starts from the very beginning.
     */
    int iterate(Arg arg) throws SQLException;
}
