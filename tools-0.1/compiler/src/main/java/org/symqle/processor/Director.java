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
        new Compiler(). process(parsedSources, model);
        outputDirectory.mkdirs();
        for (Generator generator: generators) {
            generator.generate(model, outputDirectory);
        }
    }

}
