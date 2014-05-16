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
import org.symqle.parser.ParseException;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.SymqleMethodProcessor;
import org.symqle.test.TestUtils;
import org.symqle.util.ModelUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

public class SymqleMethodProcessorTest extends TestCase {

    public void testList() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/SymqleMethod.sdl";
        final List<SyntaxTree> syntaxTree = Arrays.asList(readSyntaxTree(source));
        new SymqleMethodProcessor().process(syntaxTree, model);
        final ClassDefinition symqle = model.getClassDef("Symqle");
        assertEquals(0, symqle.getAllMethods(model).size());

        final List<MethodDefinition> methods = model.getExplicitSymqleMethods();
        assertEquals(1, methods.size());

        assertEquals(TestUtils.pureCode("static List<T> list(final SelectStatement statement, final Database database) {\n" +
                "    final SqlContext context = new SqlContext();\n" +
                "    return database.list(statement.z$sqlOfSelectStatement(context));\n" +
                "}"), TestUtils.pureCode(methods.get(0).toString()));

    }

    private SyntaxTree readSyntaxTree(String source) throws FileNotFoundException, ParseException {
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        return syntaxTree;
    }
}
