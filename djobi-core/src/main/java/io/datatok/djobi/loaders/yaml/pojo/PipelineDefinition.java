package io.datatok.djobi.loaders.yaml.pojo;

import java.io.File;
import java.util.Map;

public class PipelineDefinition {

    public Map<String, JobDefinition> jobs;

    public File definitionFile;

    public Map<String, Object> parameters;

    public ExecutorDefinition executor;

    /**
     * @since v5.0.0
     */
    public Map<String, String> labels;
}
