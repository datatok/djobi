package io.datatok.djobi.engine;

import io.datatok.djobi.plugins.report.VerbosityLevel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Execution request object of a Workflow, with workflow file path, parameters etc...
 */
public class ExecutionRequest {

    static public final String PHASES_FILTER_DEFAULT = "configuration,pre_check,run,post_check";

    /**
     * The full URL or path to the pipeline definition.
     * We just support classic file-system (no HDFS, S3 yet).
     */
    private String definitionURI;

    /**
     * @since v1.0.0
     */
    private boolean debug;

    /**
     * @since v1.0.0
     */
    private List<String> jobsFilter;

    /**
     * Store arguments.
     */
    final private Map<String, String> arguments;

    /**
     * Store run meta info (for logging).
     */
    private Map<String, String> metaDataLabels;

    /**
     * Store verbosity level.
     */
    private VerbosityLevel verbosity;

    /**
     * Store job phases.
     */
    private List<String> jobPhases;

    public static ExecutionRequest build(final String wkDefinitionPath) {
        return new ExecutionRequest(wkDefinitionPath);
    }

    public static ExecutionRequest build(final String wkDefinitionPath, final Map<String, String> args) {
        return new ExecutionRequest(args).setDefinitionURI(wkDefinitionPath);
    }

    public ExecutionRequest() {
        this.arguments = new HashMap<>();
    }

    public ExecutionRequest(final String definitionURI) {
        this();

        this.definitionURI = definitionURI;
    }

    public ExecutionRequest(final Map<String, String> arguments) {
        this();

        if (arguments != null) {
            this.setArguments(arguments);
        }

        this.debug = this.arguments.containsKey("debug");
    }

    public ExecutionRequest setJobsFilter(List<String> jobsFilter) {
        this.jobsFilter = jobsFilter;
        return this;
    }

    public List<String> getJobsFilter() {
        return jobsFilter;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public ExecutionRequest setArguments(Map<String, String> p) {
        this.arguments.putAll(p);
        return this;
    }

    public ExecutionRequest addArgument(final String k, final String v) {
        this.arguments.put(k, v);
        return this;
    }

    public String getArgument(String key) {
        return this.arguments.get(key);
    }

    public ExecutionRequest setDefinitionURI(String definitionURI) {
        this.definitionURI = definitionURI;
        return this;
    }

    public String getDefinitionURI() {
        return definitionURI;
    }

    public Map<String, String> getMetaDataLabels() {
        return metaDataLabels;
    }

    public String getMetaDataLabel(String key) {
        return metaDataLabels.get(key);
    }

    public ExecutionRequest setMetaDataLabels(Map<String, String> metaDataLabels) {
        this.metaDataLabels = metaDataLabels;

        return this;
    }

    public VerbosityLevel getVerbosity() {
        return verbosity;
    }

    public void setVerbosity(VerbosityLevel verbosity) {
        this.verbosity = verbosity;
    }

    public boolean debug() { return debug; }

    public List<String> getJobPhases() {
        if (jobPhases == null) {
            setJobPhases(Arrays.asList(PHASES_FILTER_DEFAULT.split(",")));
        }
        return jobPhases;
    }

    public ExecutionRequest setJobPhases(List<String> jobPhases) {
        this.jobPhases = jobPhases;
        return this;
    }
}
