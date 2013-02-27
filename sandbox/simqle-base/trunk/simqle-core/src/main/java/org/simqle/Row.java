/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle;

/**
 * An abstraction of a single row of a result set.
 *
 * @author Alexander Izyurov
 */
public interface Row {
    /**
     * Accesses a value slot for a column in the row by label.
     * @param label column label
     * @return the value slot
     */
    Element getValue(String label);

    /**
     * Accesses a value slot a column in the row by position.
     * @param position position in the row, numeration starts from 1
     * @return the value slot
     */
    Element getValue(int position);

}
