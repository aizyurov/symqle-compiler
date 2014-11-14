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

import org.symqle.model.AbstractTypeDefinition;
import org.symqle.model.Model;

import java.util.Collection;

/**
 * Generates test interfaces from Model.
 * @author lvovich
 */
public class TestSetGenerator extends WriterGenerator {

    /**
     * Constructs with given package name.
     * @param packageName full package name, like org.symqle.core
     */
    public TestSetGenerator(final String packageName) {
        super(packageName);
    }

    @Override
    protected final Collection<? extends AbstractTypeDefinition> processedTypes(final Model model) {
        return model.getTestInterfaces();
    }
}
