package org.symqle.generator;

import org.symqle.model.AbstractTypeDefinition;
import org.symqle.model.Model;
import org.symqle.util.Utils;

import java.io.*;

/**
 * @author lvovich
 */
public class WriterGenerator implements Generator {
    private final String packageName;

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
        for (AbstractTypeDefinition def: model.getAllTypes()) {
            final String fileName = def.getName() + ".java";
            PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(targetDir, fileName))));
            try {
                out.println("/* THIS IS GENERATED CODE. ALL CHANGES WILL BE LOST");
                out.println("   See " + def.getSourceRef() +" */");
                out.println();
                out.write("package ");
                out.write(packageName);
                out.write(";");
                out.println(Utils.LINE_BREAK);
                out.write(def.toString());
            } finally {
                out.close();
            }
        }
    }
}
