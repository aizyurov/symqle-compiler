package org.symqle.util;

import org.symqle.model.Model;
import org.symqle.parser.ParseException;
import org.symqle.parser.SimqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.ClassDeclarationProcessor;
import org.symqle.processor.GrammarException;
import org.symqle.processor.InterfaceDeclarationsProcessor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author lvovich
 */
public class ModelUtils {

    public static Model prepareModel() throws IOException, ParseException, GrammarException {
        final Model model = new Model();
        String source = "src/test-data/model/CommonClasses.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SimqleParser parser = new SimqleParser(reader);
        final SyntaxTree syntaxTree = new SyntaxTree(parser.SimqleUnit(), source);
        new InterfaceDeclarationsProcessor().process(syntaxTree, model);
        new ClassDeclarationProcessor().process(syntaxTree, model);
        return model;
    }
}
