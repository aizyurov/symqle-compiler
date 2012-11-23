/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.generator;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.simqle.model.Model;

import java.io.*;

/**
 * <br/>15.11.2011
 *
 * @author Alexander Izyurov
 */
public abstract class AbstractFreeMarkerGenerator implements Generator {
    
    public AbstractFreeMarkerGenerator() {
    }

    protected abstract String getTemplateName();
    protected abstract String getPackageName();
    protected abstract void scanModel(Model model, GeneratorCallback callback)
            throws IOException;

    public void generate(final Model model, final File destDir) throws IOException {
        final Configuration configuration= new Configuration();
        configuration.setClassForTemplateLoading(this.getClass(), "/");
        configuration.setObjectWrapper(new DefaultObjectWrapper());
        final String[] packages = getPackageName().split("\\.");
        File target = destDir;
        for (String subdir: packages) {
            target = new File(target, subdir);
        }
        final File targetDir = target;
        targetDir.mkdirs();
        final Template template = configuration.getTemplate(getTemplateName());
        template.setObjectWrapper(new DefaultObjectWrapper());
        scanModel(model,
                new GeneratorCallback() {
                    public void generateFile(String filename, Object subModel) throws IOException {
                        Writer out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, filename)));
                        try {
                            template.process(subModel, out);
                        } catch (TemplateException e) {
                            throw new RuntimeException("Internal error", e);
                        } finally {
                            out.close();
                        }
                    }
                });
        
    }

    protected interface GeneratorCallback {
        void generateFile(String filename, Object subModel) throws IOException;
    }
}
