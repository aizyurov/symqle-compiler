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
import org.symqle.processor.InterfaceJavadocProcessor;
import org.symqle.util.ModelUtils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

public class ImportsTest extends TestCase {

    public void testImports() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/Imports.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final List<SyntaxTree> syntaxTrees = Arrays.asList(new SyntaxTree(parser.SymqleUnit(), source));
        new InterfaceJavadocProcessor().process(syntaxTrees, model);
        final ClassDefinition cursorSpec = model.getClassDef("AbstractCursorSpecification");
        final String cursorSpecString = cursorSpec.toString();
        assertTrue(cursorSpecString, cursorSpecString.startsWith("import org.symqle.common.QueryBuilder;"));
        assertTrue(cursorSpecString, cursorSpecString.contains("import org.symqle.common.SqlContext;"));
        final ClassDefinition symqle = model.getClassDef("Symqle");
        final String symqleString = symqle.toString();
        assertTrue(symqleString, symqleString.startsWith("import java.util.ArrayList;"));
        assertTrue(symqleString, symqleString.contains("import java.util.List;"));
    }

}