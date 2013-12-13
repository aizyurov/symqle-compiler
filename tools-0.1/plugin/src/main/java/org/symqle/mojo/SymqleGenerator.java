package org.symqle.mojo;

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
import org.symqle.processor.Director;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Goal which touches a timestamp file.
 *
 * @goal generate
 * 
 * @phase generate-sources
 */
public class SymqleGenerator
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
     * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/generated-sources/main/"
     * @required
     */
    private File outputDirectory;

    /**
     * The directory where generated test sources are put. This directory is included
     * to project test sources.
     * @parameter expression="${testOutputDirectory}" default-value="${project.build.directory}/generated-sources/test/"
     * @required
     */
    private File testOutputDirectory;

    /**
     *  The directory where Symqle sources resides. All *.sdl files from the directory
     * are compiled
     *
     * @parameter expression="${sourceDirectory}" default-value="${basedir}/src/main/symqle"
     * @required
     */
    private File sourceDirectory;

    protected File getOutputDirectory() {
        return outputDirectory;
    }




    public void execute()
        throws MojoExecutionException, MojoFailureException
    {

        outputDirectory.mkdirs();
        testOutputDirectory.mkdirs();
        project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
        project.addTestCompileSourceRoot(testOutputDirectory.getAbsolutePath());

        if (isClean()) {
            getLog().info("All files are up to date");
            return;
        } else {
            getLog().info("Changes detected - rebuilding");
        }

        prepareDirectory(outputDirectory);
        prepareDirectory(testOutputDirectory);

        final Director director = new Director();
        try {
            director.doAll(getSources(), getOutputDirectory());
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoFailureException(e.toString());
        }

        File marker = new File(outputDirectory, "symqle.built");
        marker.delete();
        try {
            marker.createNewFile();
        } catch (IOException e) {
            // ignore - not critical: will cause full rebuild next time
        }
    }

    private boolean isClean() {
        if (!outputDirectory.exists()) {
            return false;
        }
        File marker = new File(outputDirectory, "symqle.built");
        if (!marker.exists()) {
            return false;
        }
        final long markerTs = marker.lastModified();
        for (File file : getSources()) {
            if (file.lastModified() > markerTs) {
                return false;
            }
        }
        return true;
    }

    protected File[] getSources() {
        return sourceDirectory.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(final File dir, final String name) {
                        return name.endsWith(".sdl");
                    }
                });
    }

    private void prepareDirectory(final File directory) throws MojoExecutionException {
        for (File subdir: directory.listFiles()) {
            deleteRecursively(subdir);
        }
    }

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
