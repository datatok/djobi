package io.datatok.djobi.engine;

import io.datatok.djobi.plugins.report.VerbosityLevel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipelineExecutionRequest {

    static public final String PHASES_FILTER_DEFAULT = "configuration,pre_check,run,post_check";

    /**
     * @since v1.0.0
     */
    private String pipelineDefinitionPath;

    /**
     * @since v3.6.0
     */
    private Pipeline pipeline;

    /**
     * @since v1.0.0
     */
    private boolean debug;

    /**
     * @since v1.0.0
     */
    private List<String> jobsFilter;

    /**
     * Store raw args.
     *
     *  @since v1.0.0
     */
    private Map<String, String> raw;

    /**
     * Store run meta info (for logging).
     *
     * @since v2.2.8
     */
    private Map<String, String> meta;

    /**
     * Store verbosity level.
     *
     * @since v2.2.9 - #34
     */
    private VerbosityLevel verbosity;

    /**
     * Store job phases.
     *
     * @since v3.6.0
     */
    private List<String> jobPhases;

    public static PipelineExecutionRequest build(final String pipelineDefinitionPath) {
        return new PipelineExecutionRequest(pipelineDefinitionPath);
    }

    public static PipelineExecutionRequest build(final String pipelineDefinitionPath, final Map<String, String> args) {
        return new PipelineExecutionRequest(args).setPipelineDefinitionPath(pipelineDefinitionPath);
    }

    public PipelineExecutionRequest(final String pipelineDefinitionPath) {
        this.pipelineDefinitionPath = pipelineDefinitionPath;
    }

    public PipelineExecutionRequest(final Map<String, String> args) {
        if (args == null) {
            this.raw = new HashMap<>();
        } else {
            this.raw = args;
        }

        this.debug = this.raw.containsKey("debug");
    }

    public PipelineExecutionRequest() {
    }

    public PipelineExecutionRequest addArgument(final String k, final String v) {
        if (this.raw == null) {
            this.raw = new HashMap<>();
        }

        this.raw.put(k, v);
        return this;
    }

    public PipelineExecutionRequest setJobsFilter(List<String> jobsFilter) {
        this.jobsFilter = jobsFilter;
        return this;
    }

    public List<String> getJobsFilter() {
        return jobsFilter;
    }

    public Map<String, String> getRaw() {
        return raw;
    }

    public PipelineExecutionRequest setRaw(Map<String, String> raw) {
        this.raw = raw;
        return this;
    }

    public PipelineExecutionRequest setPipelineDefinitionPath(String pipelineDefinitionPath) {
        this.pipelineDefinitionPath = pipelineDefinitionPath;
        return this;
    }

    public String getPipelineDefinitionPath() {
        return pipelineDefinitionPath;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public PipelineExecutionRequest setMeta(Map<String, String> meta) {
        this.meta = meta;

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

    public Pipeline getPipeline() {
        return pipeline;
    }

    public PipelineExecutionRequest setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }
}
