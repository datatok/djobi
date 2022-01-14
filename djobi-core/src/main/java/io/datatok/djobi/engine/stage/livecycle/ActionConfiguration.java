package io.datatok.djobi.engine.stage.livecycle;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract public class ActionConfiguration {

    protected TemplateUtils templateUtils;

    protected Bag stageConfiguration;

    protected Job job;

    static public <T> T get(Class<T> clazz, final Bag stageConfiguration, final Job job, final TemplateUtils templateUtils) throws Exception
    {
        Constructor<T> constructor = clazz.getConstructor(Bag.class, Job.class, TemplateUtils.class);

        return constructor.newInstance(stageConfiguration, job, templateUtils);
    }

    static public <T> T get(Class<T> clazz, final Bag stageConfiguration, final Stage stage, final TemplateUtils templateUtils) throws Exception
    {
        return get(clazz, stageConfiguration, stage.getJob(), templateUtils);
    }

    public ActionConfiguration(final Bag stageConfiguration, final Job job, final TemplateUtils templateUtils) {
        this.stageConfiguration = stageConfiguration;
        this.templateUtils = templateUtils;
        this.job = job;
    }

    protected String render(final String key, final String def) {
        if (stageConfiguration != null && stageConfiguration.containsKey(key)) {
            final String value = stageConfiguration.getString(key);

            return renderValue(value);
        }

        return def;
    }

    protected String render(final String key) {
        return render(key, null);
    }

    protected String renderValue(final String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        if (value.startsWith("$")) {
            return (String) templateUtils.getAsVariable(job, value);
        }

        return templateUtils == null || job == null
                ? value
                : templateUtils.render(value, job)
                ;
    }

    protected String renderPath(final String key, final String def) {
        String path = render(key, def);

        if (path == null) {
            return null;
        }

        if (path.startsWith(".")) {
            path = job.getPipeline().resolvePath(path);
        }

        return path;
    }

    protected String renderPath(final String key) {
        return renderPath(key, null);
    }

    protected Map<String, String> renderMap(final String key) {
        return renderMap(key, false);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> renderObjectsMap(final String key) {
        if (stageConfiguration != null && stageConfiguration.containsKey(key)) {
            final Object value = stageConfiguration.get(key);

            if (value instanceof String && ( (String) value).startsWith("{{@")) {
                return (Map<String, Object>) templateUtils.renderOption( (String) value, job);
            }

            if (value instanceof Map) {
                return (Map<String, Object>) value;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> renderMap(final String key, final boolean renderValues) {
        if (stageConfiguration != null && stageConfiguration.containsKey(key)) {
            final Object value = stageConfiguration.get(key);

            if (value instanceof String && ( (String) value).startsWith("{{@")) {
                return (Map<String, String>) templateUtils.renderOption( (String) value, job);
            }

            if (value instanceof Map) {
                Map<String, String> newMap = new HashMap<>((Map<String, String>) value);

                if (renderValues) {
                    newMap.replaceAll((k, v) -> renderValue(v));
                }

                return newMap;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    protected List<String> renderList(final String key) {
        if (stageConfiguration != null && stageConfiguration.containsKey(key)) {
            final Object value = stageConfiguration.get(key);

            if (value instanceof String && ( (String) value).startsWith("$")) {
                return (List) templateUtils.getAsVariable(this.job, (String) value);
            }

            if (value instanceof String && ( (String) value).startsWith("{{@")) {
                return (List) templateUtils.renderOption( (String) value, job);
            }

            final List<String> original = stageConfiguration.getStringList(key);

            if (templateUtils == null || job == null) {
                return original;
            } else {
                return original.stream().map(entry -> templateUtils.render(entry, job)).collect(Collectors.toList());
            }
        }

        return null;
    }

    protected boolean has(final String key) {
        return this.stageConfiguration.containsKey(key);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
