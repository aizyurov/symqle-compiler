package org.symqle.util;

import org.symqle.model.Model;
import org.symqle.parser.ParseException;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.ClassDeclarationProcessor;
import org.symqle.processor.GrammarException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

/**
 * @author lvovich
 */
public class ModelUtils {

    public static Model prepareModel() throws IOException, ParseException, GrammarException {
        final Model model = new Model();
        String source = "src/test-data/model/CommonClasses.sdl";
        Reader reader = new InputStreamReader(new FileInputStream(source));
        SymqleParser parser = new SymqleParser(reader);
        final List<SyntaxTree> syntaxTrees = Arrays.asList(new SyntaxTree(parser.SymqleUnit(), source));
        new ClassDeclarationProcessor().process(syntaxTrees, model);
        return model;
    }
}
