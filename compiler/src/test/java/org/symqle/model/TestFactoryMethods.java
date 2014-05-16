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
import org.symqle.processor.FinalizationProcessor;
import org.symqle.test.TestUtils;
import org.symqle.util.ModelUtils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

public class TestFactoryMethods extends TestCase {

    public void testSimple() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/SimpleFactoryMethod.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final List<SyntaxTree> syntaxTrees = Arrays.asList(new SyntaxTree(parser.SymqleUnit(), source));
        new FinalizationProcessor().process(syntaxTrees, model);
        final ClassDefinition symqle = model.getClassDef("Symqle");
        final MethodDefinition forUpdate = symqle.getDeclaredMethodBySignature("forUpdate(CursorSpecification)");
        assertNotNull(forUpdate);
        assertEquals("AbstractSelectStatement", forUpdate.getResultType().getSimpleName());

        final ClassDefinition abstractSelectStatement = model.getClassDef("AbstractSelectStatement");
        final MethodDefinition show = abstractSelectStatement.getDeclaredMethodBySignature("show()");
        assertEquals(TestUtils.pureCode("public final String show() { return Symqle.show(this); }"),
                TestUtils.pureCode(show.toString()));

    }

}
