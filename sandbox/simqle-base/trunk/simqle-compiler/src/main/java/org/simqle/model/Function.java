/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

/**
 * <br/>17.11.2011
 *
 * @author Alexander Izyurov
 */
public interface Function<Res, Arg> {
    Res apply(Arg arg);
}
