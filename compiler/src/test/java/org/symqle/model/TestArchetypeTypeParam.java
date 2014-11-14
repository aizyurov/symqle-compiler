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
import org.symqle.processor.GrammarException;
import org.symqle.processor.InterfaceDeclarationsProcessor;
import org.symqle.util.ModelUtils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

public class TestArchetypeTypeParam extends TestCase {

    public void testNoQueryParam() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/ArchetypeNoQueryParam.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        try {
            new InterfaceDeclarationsProcessor().process(Arrays.asList(syntaxTree), model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage().startsWith("Query archetype requires 1 type parameter, found: 0"));
        }
    }

    public void testSqlParam() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/ArchetypeSqlParam.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        try {
            new InterfaceDeclarationsProcessor().process(Arrays.asList(syntaxTree), model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage().startsWith("SqlBuilder archetype does not take type parameters, found: 1"));
        }
    }

    public void testIllegalMethodName() throws Exception {
        final Model model = ModelUtils.prepareModel();
        String source = "src/test-data/model/ArchetypeIllegalMethodName.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SymqleUnit(), source);
        try {
            new InterfaceDeclarationsProcessor().process(Arrays.asList(syntaxTree), model);
            fail("GrammarException expected");
        } catch (GrammarException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Prefix \"z$sqlOf\" is reserved for generated methods"));
        }
    }

}
