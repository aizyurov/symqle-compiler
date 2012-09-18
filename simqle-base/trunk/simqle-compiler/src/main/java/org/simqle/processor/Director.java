/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.processor;

import org.simqle.generator.Generator;
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
    private Processor[] processors = {
            new InterfaceDeclarationsProcessor(),
            new ClassDeclarationProcessor(),
            new ProductionDeclarationProcessor(),
    };

    private final MimicsProcessor mimicsProcessor = new MimicsProcessor();

    private Generator[] generators = {};

    public void doAll(final File[] sources, final File outputDirectory) throws IOException, GrammarException, ParseException, ModelException {
        List<SyntaxTree> parsedSources = new ArrayList<SyntaxTree>(sources.length);
        for (File source: sources) {
            Reader reader = new InputStreamReader(new FileInputStream(source));
            try {
                SimqleParser parser = new SimqleParser(reader);
                parsedSources.add(new SyntaxTree(parser.SimqleUnit(), source.getName()));
            } finally {
                reader.close();
            }
        }
        Model model = new Model();
        for (Processor processor: processors) {
            for (SyntaxTree source: parsedSources) {
                processor. process(source, model);
            }
        }
        mimicsProcessor.process(model);
        for (Generator generator: generators) {
            generator.generate(model, outputDirectory);
        }
    }

}
