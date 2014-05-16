/*
   Copyright 2011-2014 Alexander Izyurov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.package org.symqle.common;
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
