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

package org.symqle.processor;

import org.symqle.generator.CoreGenerator;
import org.symqle.generator.TestSetGenerator;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.parser.ParseException;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for compiler plugin. Parses all sources and generates all code.
 * @author Alexander Izyurov
 */
public class SymqleCompiler {

    // processors sequence is define inside processors; currently:
    /*

       InterfaceDeclarationsProcessor
       ClassDeclarationProcessor
       ProductionProcessor
       SymqleMethodProcessor
       ImplicitConversionProcessor
       InheritanceProcessor
       InterfaceEnhancer
       ImplementationProcessor
       ClassEnhancer
       InterfaceJavadocProcessor,
       TestClassesProcessor,
       FinalizationProcessor
     */


    /**
     * Parses all sources and generates all code.
     * @param sources source sdl files
     * @param outputDirectory output directory for production code
     * @param testOutputDirectory putput directory for test code
     * @throws IOException error reading/writing
     * @throws GrammarException semantic error
     * @throws ParseException syntax error
     * @throws ModelException semantic error not bound to any specific source location
     */
    public final void doAll(final File[] sources,
                            final File outputDirectory,
                            final File testOutputDirectory)
            throws IOException, GrammarException, ParseException, ModelException {
        List<SyntaxTree> parsedSources = new ArrayList<SyntaxTree>(sources.length);
        for (File source: sources) {
            Reader reader = new InputStreamReader(new FileInputStream(source), "UTF-8");
            try {
                SymqleParser parser = new SymqleParser(reader);
                parsedSources.add(new SyntaxTree(parser.SymqleUnit(), source.getName()));
            } catch (ParseException e) {
                Log.info(e.getMessage() + " [" + source.getName() + "]");
                throw e;
            } finally {
                reader.close();
            }
        }
        final Model model = new Model();
        new FinalizationProcessor().process(parsedSources, model);
        if (!outputDirectory.mkdirs() && !outputDirectory.isDirectory()) {
            throw new IOException("Failed to create " + outputDirectory);
        }
        if (!testOutputDirectory.mkdirs() && !testOutputDirectory.isDirectory()) {
            throw new IOException("Failed to create " + testOutputDirectory);
        }
        new CoreGenerator("org.symqle.sql").generate(model, outputDirectory);
        new TestSetGenerator("org.symqle.testset").generate(model, testOutputDirectory);
    }

}
