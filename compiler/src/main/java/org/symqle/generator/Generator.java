/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.generator;

import org.symqle.model.Model;

import java.io.File;
import java.io.IOException;

/**
 * Common interface for Java sources generation from Model.
 *
 * @author Alexander Izyurov
 */
public interface Generator {
    /**
     * Generate java sources from model.
     * @param model collection of class and interface definitions
     * @param destDir generated sources directory (top level).
     * @throws IOException write failure
     */
    void generate(Model model, File destDir) throws IOException;
}
