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
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.ClassDeclarationProcessor;

import java.io.FileReader;
import java.util.Arrays;

public class ObjectTest extends TestCase {
    public void testObjectClass() throws Exception {
        Model model = new Model();
        SymqleParser parser = new SymqleParser(new FileReader("src/test-data/model/Object.sdl"));
        SyntaxTree node = new SyntaxTree(parser.SymqleUnit(), "Object.sdl");
        new ClassDeclarationProcessor().process(Arrays.asList(node), model);

        final ClassDefinition object = model.getClassDef("Object");
        assertNotNull(object);
        assertNotNull(object.getMethodBySignature("equals(Object)", model));
        assertNotNull(object.getDeclaredMethodBySignature("equals(Object)"));
    }

}
