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

package org.symqle.model;

import junit.framework.TestCase;
import org.symqle.util.ModelUtils;

import java.util.List;

public class ModelTest extends TestCase {

    public void testConstructor() throws Exception {
        final Model model = ModelUtils.prepareModel();
        assertEquals(2, model.getAllClasses().size());
        assertEquals(1, model.getAllInterfaces().size());
        final ClassDefinition simqle = model.getClassDef("Symqle");
        assertNotNull(simqle);
        final ClassDefinition genericDialect = model.getClassDef("GenericDialect");
        final InterfaceDefinition dialect = model.getInterface("Dialect");
        final List<Type> interfaces = genericDialect.getImplementedInterfaces();
        assertEquals(1, interfaces.size());
        assertEquals(dialect.getType(), interfaces.get(0));

    }

}
