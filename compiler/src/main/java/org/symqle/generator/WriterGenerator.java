package org.symqle.generator;

import org.symqle.model.AbstractTypeDefinition;
import org.symqle.model.Model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * @author lvovich
 */
public abstract class WriterGenerator implements Generator {
    protected final String packageName;

    public WriterGenerator(final String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void generate(final Model model, final File destDir) throws IOException {
        final String[] packages = packageName.split("\\.");
        File target = destDir;
        for (String subdir: packages) {
            target = new File(target, subdir);
        }
        final File targetDir = target;
        targetDir.mkdirs();
        for (AbstractTypeDefinition def: processedTypes(model)) {
            final String fileName = def.getName() + ".java";
            PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(targetDir, fileName))));
            try {
                out.println("/* THIS IS GENERATED CODE. ALL CHANGES WILL BE LOST");
                out.println("   See " + def.getSourceRef() +" */");
                out.println();
                out.write("package ");
                out.write(packageName);
                out.write(";");
                out.println();
                out.write(def.toString());
            } finally {
                out.close();
            }
        }
    }

    protected abstract Collection<? extends AbstractTypeDefinition> processedTypes(Model model);
}
