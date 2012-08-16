package org.simqle.mojo;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.*;

/**
 * Goal which touches a timestamp file.
 *
 * @goal generate
 * 
 * @phase generate-sources
 */
public class MyMojo
    extends AbstractMojo
{
    /**
     * The current Maven project.
     *
     * @parameter default-value="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * The directory where generated sources are put. This directory is included
     * to project sources.
     * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/generated-sources/main/simqle"
     * @required
     */
    private File outputDirectory;

    /**
     * The directory where generated test sources are put. This directory is included
     * to project test sources.
     * @parameter expression="${testOutputDirectory}" default-value="${project.build.directory}/generated-sources/test/simqle"
     * @required
     */
    private File testOutputDirectory;

    /**
     *  The directory where Simqle sourcd resides. All *.sql files from the directory
     * are compiled
     *
     * @parameter expression="${sourceDirectory}" default-value="${basedir}/src/main/simqle"
     * @required
     */
    private File sourceDirectory;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        prepareDirectory(outputDirectory);
        prepareDirectory(testOutputDirectory);

//        final Model model = new Model();
//        final GrammarParser modeler = new GrammarParser();
//        for (File source: sourceDirectory.listFiles(new FileFilter() {
//            public boolean accept(File pathname) {
//                return pathname.isFile() && pathname.getPairName().endsWith(".sdl");
//            }
//        })) {
//            final SyntaxTree syntaxTree = parseSourceFile(source);
//            try {
//                modeler.process(model, syntaxTree);
//            } catch (GrammarException e) {
//                throw new MojoFailureException(e.getMessage());
//            }
//        }
//        CodeGenerator codeGenerator = new CodeGenerator();
//        try {
//            codeGenerator.generate(outputDirectory, model);
//            codeGenerator.generateTestSources(testOutputDirectory, model);
//        } catch (IOException e) {
//            throw new MojoExecutionException("Code generation failed", e);
//        } catch (TemplateException e) {
//            throw new MojoExecutionException("Code generation failed", e);
//        }
        project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
        project.addTestCompileSourceRoot(testOutputDirectory.getAbsolutePath());
    }

    private void prepareDirectory(final File directory) throws MojoExecutionException {
        directory.mkdirs();
        for (File subdir: directory.listFiles()) {
            deleteRecursively(subdir);
        }
    }

//    private SyntaxTree parseSourceFile(File source) throws MojoExecutionException, MojoFailureException {
//        try {
//            final FileInputStream inputStream = new FileInputStream(source);
//            try {
//                SimqleParser simqleParser = new SimqleParser(inputStream);
//                final SimpleNode start = simqleParser.Start();
//                return new SyntaxTree(start);
//            } finally {
//                inputStream.close();
//            }
//        } catch (ParseException e) {
//            throw new MojoFailureException(e.getMessage());
//        } catch (IOException e) {
//            throw new MojoExecutionException("Code generation failed", e);
//        }
//    }


    private static void deleteRecursively(File f) throws MojoExecutionException {
        if (f.isDirectory()) {
            for (File file: f.listFiles()) {
                deleteRecursively(file);
            }
        }
        if (!f.delete()) {
            throw new MojoExecutionException("Failed to delete "+f);
        }
    }
}
