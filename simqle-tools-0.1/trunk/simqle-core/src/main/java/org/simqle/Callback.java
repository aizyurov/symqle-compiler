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
public interface Callback<Arg, Ex extends Exception> {

    /**
     * Called by iterating object in a loop. The iterating object passes values of arg one by one.
     * @param arg the callback argument
     * @throws Ex callback error
     * @throws BreakException signals that iterations must be stopped
     */
    void iterate(Arg arg) throws Ex, BreakException;

    public static class BreakException extends Exception{
    }
}
