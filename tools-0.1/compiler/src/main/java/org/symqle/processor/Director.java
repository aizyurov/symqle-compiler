/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.processor;

import org.symqle.generator.CoreGenerator;
import org.symqle.generator.TestSetGenerator;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.parser.ParseException;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * <br/>14.11.2011
 *
 * @author Alexander Izyurov
 */
public class Director {

    // processors sequence is define inside processors; currentkry:
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
       TestClassesProcessor
     */



    public void doAll(final File[] sources, final File outputDirectory, final File testOutputDirectory) throws IOException, GrammarException, ParseException, ModelException {
        List<SyntaxTree> parsedSources = new ArrayList<SyntaxTree>(sources.length);
        for (File source: sources) {
            Reader reader = new InputStreamReader(new FileInputStream(source));
            try {
                SymqleParser parser = new SymqleParser(reader);
                parsedSources.add(new SyntaxTree(parser.SymqleUnit(), source.getName()));
            } catch (ParseException e) {
                System.err.println(e.getMessage()+" ["+source.getName()+"]");
                throw e;
            } finally {
                reader.close();
            }
        }
        Model model = new Model();
        new TestClassesProcessor(). process(parsedSources, model);
        outputDirectory.mkdirs();
        testOutputDirectory.mkdirs();
        new CoreGenerator("org.symqle.sql").generate(model, outputDirectory);
        new TestSetGenerator("org.symqle.testset").generate(model, testOutputDirectory);
    }

}
