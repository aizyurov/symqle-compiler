/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.model;

/**
 * Indicates invalid state or Model or attempt to make model invalid.
 *
 * @author Alexander Izyurov
 */
public class ModelException extends Exception {
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public ModelException(final String message) {
        super(message);
    }
}
