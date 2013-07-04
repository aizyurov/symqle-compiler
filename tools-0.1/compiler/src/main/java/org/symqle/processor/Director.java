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
    private Processor[] step1processors = {
            new InterfaceDeclarationsProcessor(),
            new InterfaceValidator(),
            new ClassDeclarationProcessor(),
    };

    private ModelProcessor[] step1modelProcessors = {
            new AbstractMethodsProcessor()
    };

    private Processor[] step2processors = {
            new ProductionDeclarationProcessor(),
            new SymqleMethodProcessor(),
            new ImplicitDeclarationProcessor()
    };


    private ModelProcessor[] step2modelProcessors = {
            new InheritanceProcessor(),
            new ClassEnhancer(),
            new InterfaceJavadocProcessor()
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
        for (Processor processor: step1processors) {
            for (SyntaxTree source: parsedSources) {
                processor. process(source, model);
            }
        }
        for (ModelProcessor modelProcessor: step1modelProcessors) {
            modelProcessor.process(model);
        }
        for (Processor processor: step2processors) {
            for (SyntaxTree source: parsedSources) {
                processor. process(source, model);
            }
        }
        for (ModelProcessor modelProcessor: step2modelProcessors) {
            modelProcessor.process(model);
        }
        outputDirectory.mkdirs();
        for (Generator generator: generators) {
            generator.generate(model, outputDirectory);
        }
    }

}
