package io.datatok.djobi.loaders.yaml.pojo;

import java.io.File;
import java.util.List;
import java.util.Map;

public class WorkflowDefinition {

    public List<JobDefinition> jobs;

    public File definitionFile;

    public Map<String, Object> parameters;

    public ExecutorDefinition executor;

    /**
     * @since v5.0.0
     */
    public Map<String, String> labels;
}
