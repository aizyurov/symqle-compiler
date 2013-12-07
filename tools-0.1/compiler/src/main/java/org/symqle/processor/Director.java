/*
* Copyright Alexander Izyurov 2010
*/
package org.symqle.processor;

import org.symqle.generator.Generator;
import org.symqle.generator.WriterGenerator;
import org.symqle.model.Model;
import org.symqle.model.ModelException;
import org.symqle.parser.ParseException;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <br/>14.11.2011
 *
 * @author Alexander Izyurov
 */
public class Director {
    private Processor[] processors = {
            new InterfaceDeclarationsProcessor(),
            new ClassDeclarationProcessor(),
            new DialectCompilationProcessor(),
            new ProductionProcessor(),
            new SymqleMethodProcessor(),
            new ImplicitDeclarationProcessor(),
            // by this time all Symqle methods are in symqleTemplate, some of them abstract
            // initial (not Symqle ) methods are explicitly declared abstract
            new UnsafeMethodsMarker(),

            new InterfaceEnhancer(),
            // all explicit methods are declared in interfaces
            new InitialImplementationProcessor(),

            new ProductionImplementationProcessor(),
            // all production methods are implemented in Symqle
            // or moved from symqleTemplate if already implemented
            new InheritanceProcessor(),
            new ClassEnhancer()
    };

    private final Generator[] generators = {
            new WriterGenerator("org.symqle.sql")
    };

    public void doAll(final File[] sources, final File outputDirectory) throws IOException, GrammarException, ParseException, ModelException {
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
        for (Processor processor: processors) {
            processor. process(parsedSources, model);
        }
        outputDirectory.mkdirs();
        for (Generator generator: generators) {
            generator.generate(model, outputDirectory);
        }
    }

}
