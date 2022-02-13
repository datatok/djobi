package io.datatok.djobi.engine;

import io.datatok.djobi.plugins.report.VerbosityLevel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represent which pipeline and how, djobi must run.
 */
public class PipelineExecutionRequest {

    static public final String PHASES_FILTER_DEFAULT = "configuration,pre_check,run,post_check";

    /**
     * The full URL or path to the pipeline definition.
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

    public static PipelineExecutionRequest build(final String pipelineDefinitionPath) {
        return new PipelineExecutionRequest(pipelineDefinitionPath);
    }

    public static PipelineExecutionRequest build(final String pipelineDefinitionPath, final Map<String, String> args) {
        return new PipelineExecutionRequest(args).setDefinitionURI(pipelineDefinitionPath);
    }

    public PipelineExecutionRequest() {
        this.arguments = new HashMap<>();
    }

    public PipelineExecutionRequest(final String definitionURI) {
        this();

        this.definitionURI = definitionURI;
    }

    public PipelineExecutionRequest(final Map<String, String> arguments) {
        this();

        if (arguments != null) {
            this.setArguments(arguments);
        }

        this.debug = this.arguments.containsKey("debug");
    }

    public PipelineExecutionRequest setJobsFilter(List<String> jobsFilter) {
        this.jobsFilter = jobsFilter;
        return this;
    }

    public List<String> getJobsFilter() {
        return jobsFilter;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public PipelineExecutionRequest setArguments(Map<String, String> p) {
        this.arguments.putAll(p);
        return this;
    }

    public PipelineExecutionRequest addArgument(final String k, final String v) {
        this.arguments.put(k, v);
        return this;
    }

    public String getArgument(String key) {
        return this.arguments.get(key);
    }

    public PipelineExecutionRequest setDefinitionURI(String definitionURI) {
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

    public PipelineExecutionRequest setMetaDataLabels(Map<String, String> metaDataLabels) {
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

    public PipelineExecutionRequest setJobPhases(List<String> jobPhases) {
        this.jobPhases = jobPhases;
        return this;
    }
}
