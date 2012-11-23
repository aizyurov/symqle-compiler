/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.generator;

import org.simqle.model.Model;

import java.io.File;
import java.io.IOException;

/**
 * <br/>15.11.2011
 *
 * @author Alexander Izyurov
 */
public interface Generator {
    void generate(Model model, File destDir) throws IOException;
}
