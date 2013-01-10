/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.generator.Generator;
import org.simqle.generator.WriterGenerator;
import org.simqle.model.Model;
import org.simqle.model.ModelException;
import org.simqle.parser.ParseException;
import org.simqle.parser.SimqleParser;
import org.simqle.parser.SyntaxTree;

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
            new SimqleMethodProcessor(),
            new ImplicitDeclarationProcessor()
    };


    private ModelProcessor[] step2modelProcessors = {
            new InheritanceProcessor(),
            new ClassEnhancer()
    };

    private final Generator[] generators = {
            new WriterGenerator("org.simqle.sql")
    };

    public void doAll(final File[] sources, final File outputDirectory) throws IOException, GrammarException, ParseException, ModelException {
        List<SyntaxTree> parsedSources = new ArrayList<SyntaxTree>(sources.length);
        for (File source: sources) {
            Reader reader = new InputStreamReader(new FileInputStream(source));
            try {
                SimqleParser parser = new SimqleParser(reader);
                parsedSources.add(new SyntaxTree(parser.SimqleUnit(), source.getName()));
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
