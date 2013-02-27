package org.simqle.generation;

import junit.framework.TestCase;
import org.simqle.generator.ClassGenerator;
import org.simqle.generator.FactoryGenerator;
import org.simqle.generator.Generator;
import org.simqle.generator.GenericFactoryGenerator;
import org.simqle.processor.Director;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: lvovich
 * Date: 24.09.12
 * Time: 12:50
 * To change this template use File | Settings | File Templates.
 */
public class TestClassGeneration extends TestCase {

    public void testClassGeneration() throws Exception {
        Director director = new Director(new Generator[]{new ClassGenerator()});
        director.doAll(new File[]{new File("src/test-data/interface-generation/ClassGeneration.sdl")},
                new File("target/test-generated-sources-2"));

    }

    public void testSqlFactory() throws Exception {
        Director director = new Director(new Generator[]{new FactoryGenerator()});
        director.doAll(new File[]{new File("src/test-data/interface-generation/ClassGeneration.sdl")},
                new File("target/test-generated-sources-3"));
    }

    public void testGenericSqlFactory() throws Exception {
        Director director = new Director(new Generator[]{new GenericFactoryGenerator()});
        director.doAll(new File[]{new File("src/test-data/interface-generation/ClassGeneration.sdl")},
                new File("target/test-generated-sources-3"));
    }

    public void testSelectStatement() throws Exception {
        Director director = new Director(new Generator[]{new ClassGenerator()});
        director.doAll(new File[]{new File("src/test-data/interface-generation/SelectStatement.sdl"), new File("src/test-data/interface-generation/Scalar.sdl")},
                new File("target/test-generated-sources-4"));

    }

}
