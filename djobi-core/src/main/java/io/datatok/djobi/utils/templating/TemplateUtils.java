package io.datatok.djobi.utils.templating;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import io.datatok.djobi.configuration.Configuration;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.utils.MyMapUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
public class TemplateUtils {

    @Inject
    private Configuration configuration;

    /**
     * @since v2
     * @param key
     * @return
     */
    public Object getConfigurationObject(String key) {
        return configuration.getAnyRef(key);
    }

    public Object renderOption(String snippet, final Job job) {
        if (snippet.startsWith("{{@")) {
            final String configKey = snippet.substring(3, snippet.length() - 2);

            return configuration.getValue(configKey).unwrapped();
        }

        return render(snippet, job);
    }

    public String render(String snippet) {
        return renderTemplate(false, snippet);
    }

    public String render(String snippet, final List<Object> templateDataBuffer) {
        return renderTemplate(false, snippet, templateDataBuffer);
    }

    public String render(String snippet, final Job job) {
        return renderTemplate(false, snippet, Arrays.asList(MyMapUtils.map("pipeline", job.getPipeline(), "job", job, "data", job.getData(), "args", job.getParameters(), "context", job.getParameters()), job.getParameters()));
    }

    public String renderTemplate(boolean isFile, String template) {
        return renderTemplate(isFile, template, new ArrayList<>());
    }

    public String renderTemplate(boolean isFile, String template, final List<Object> templateDataBuffer) {
        final DefaultMustacheFactory mf = new DefaultMustacheFactory();
        final Writer writerMessage = new StringWriter();
        final Mustache mustache;

        mf.setObjectHandler(new MyReflectionObjectHander());

        if (template == null) {
            return null;
        }

        if (isFile) {
            mustache = mf.compile(template);
        } else {
            mustache = mf.compile(new StringReader(template), "template");
        }

        final ArrayList<Object> templateData = new ArrayList<>(templateDataBuffer);

        if (configuration != null) {
            /** "config." is deprecated, use "env." **/
            templateData.add(MyMapUtils.map("config", configuration.root().unwrapped()));
            templateData.add(MyMapUtils.map("env", MyMapUtils.wrapByMissingKeyException(configuration.root().unwrapped())));
        }

        mustache.execute(writerMessage, templateData);

        try {
            writerMessage.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return writerMessage.toString();
    }

    /**
     * @since v3.9.0
     *
     * Get field as variable:
     * - job parameter
     * - config
     * - env
     *
     * @param key
     * @return
     */
    public Object getAsVariable(Job job, String key) {
        // Trim "$"
        key = key.substring(1);

        if (job.getParameters().containsKey(key)) {
            return job.getParameters().get(key).getValue();
        }

        if (key.startsWith("context")) {
            String key2 = key.replace("context.", "");
            if (job.getParameters().containsKey(key2)) {
                return job.getParameters().get(key2).getValue();
            }
        }

        if (key.startsWith("env.")) {
            return this.getConfigurationObject(key.replace("env.", ""));
        }

        return key;
    }
}
