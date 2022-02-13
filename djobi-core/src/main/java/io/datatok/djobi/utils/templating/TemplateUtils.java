package io.datatok.djobi.utils.templating;

import com.hubspot.jinjava.Jinjava;
import io.datatok.djobi.configuration.Configuration;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.utils.MyMapUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

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
        return renderTemplate(snippet);
    }

    public String render(String snippet, final Job job) {
        Map<String, Object> templateData = new HashMap<>();

        if (job.getParameters() != null) {
            templateData.putAll(job.getParameters());
        }

        templateData.putAll(MyMapUtils.map(
            "pipeline", job.getPipeline(),
            "job", job,
            "data", job.getData(),
            "parameters", job.getParameters(),
            "context", job.getParameters()
        ));

        return renderTemplate(snippet, templateData);
    }

    public String renderTemplate(String template) {
        return renderTemplate(template, new HashMap<>());
    }

    public String renderTemplate(String template, Map<String, ?> inTemplateData) {
        final Jinjava jinjava = new Jinjava();

        if (template == null) {
            return null;
        }

        //final Map<String, ?> templateData = new HashMap<>();

        if (configuration != null) {
            jinjava.getGlobalContext().put("env", MyMapUtils.wrapByMissingKeyException(configuration.root().unwrapped()));
            jinjava.getGlobalContext().put("config", configuration.root().unwrapped());
        }

        return jinjava.render(template, inTemplateData);
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
