package org.simqle.generator;

import org.simqle.model.AbstractTypeDefinition;
import org.simqle.model.Model;
import org.simqle.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

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
            Writer out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, fileName)));
            try {
                out.write("package ");
                out.write(packageName);
                out.write(";");
                out.write(Utils.LINE_BREAK);
                out.write(def.toString());
            } finally {
                out.close();
            }
        }
    }
}
