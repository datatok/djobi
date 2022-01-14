package io.datatok.djobi.engine.stage;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.enums.ExecutionStatus;
import io.datatok.djobi.engine.phases.StagePhaseMetaData;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.plugins.logging.StageLog;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.Dumpable;
import io.datatok.djobi.utils.MyMapUtils;
import io.datatok.djobi.utils.Timeline;
import io.datatok.djobi.utils.interfaces.Labelized;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stage implements Dumpable, Labelized {

    final protected StageLog log;

    final private Map<String, Object> metrics = new HashMap<>();

    private Job job;

    private String name;

    private String kind;

    private Boolean enabled = true;

    private Boolean allowFailure = false;

    private Bag spec;

    private String condition;

    /**
     * @since v3.16.0
     */
    private final List<StagePhaseMetaData> phases = new ArrayList<>();

    /**
     * @since v3.1.0
     */
    private Boolean preCheckEnabled = true;

    /**
     * @since v3.1.0
     */
    private Boolean postCheckEnabled = true;

    /**
     * @since v3.1.0
     */
    private CheckResult preCheck = CheckResult.todo();

    /**
     * @since v3.1.0
     */
    private CheckResult postCheck = CheckResult.todo();

    /**
     * @since v2.2.1
     */
    private ExecutionStatus executionStatus = ExecutionStatus.NO;

    /**
     * @since v2.2.8
     */
    private ActionConfiguration parameters;

    /**
     * Measure stage duration.
     *
     * @since v3.5.0
     */
    final private Timeline timeline = new Timeline();

    /**
     * Hold labels for reporting/filtering.
     *
     * @since v5.0.0
     */
    public Map<String, String> labels;

    public Stage() {
        this.log = new StageLog();
    }

    public Stage(final Bag config) {
        this();
        this.spec = config;
    }

    public String getUid() {
        return this.getJob().getUid() + "-stage" + this.getJob().getStages().indexOf(this);
    }

    public String getName() {
        return name;
    }

    public Stage setName(String name) {
        this.name = name;

        return this;
    }

    public String getKind() {
        return kind;
    }

    public Stage setKind(String kind) {
        this.kind = kind;

        return this;
    }

    public Bag getSpec() {
        return spec;
    }

    public Stage setSpec(Bag spec) {
        this.spec = spec;

        return this;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Stage setEnabled(Boolean enabled) {
        this.enabled = enabled;

        return this;
    }

    public Boolean getAllowFailure() {
        return allowFailure;
    }

    public Stage setAllowFailure(Boolean allowFailure) {
        this.allowFailure = allowFailure;

        return this;
    }

    public Job getJob() {
        return job;
    }

    public Stage setJob(Job job) {
        this.job = job;

        return this;
    }

    public CheckResult getPreCheck() {
        return preCheck;
    }

    public Stage setPreCheck(CheckResult preCheck) {
        this.preCheck = preCheck;
        return this;
    }

    public CheckResult getPostCheck() {
        return postCheck;
    }

    public Stage setPostCheck(CheckResult postCheck) {
        this.postCheck = postCheck;
        return this;
    }

    public Boolean getPreCheckEnabled() {
        return preCheckEnabled;
    }

    public Stage setPreCheckEnabled(Boolean preCheckEnabled) {
        this.preCheckEnabled = preCheckEnabled;
        return this;
    }

    public Boolean getPostCheckEnabled() {
        return postCheckEnabled;
    }

    public Stage setPostCheckEnabled(Boolean postCheckEnabled) {
        this.postCheckEnabled = postCheckEnabled;
        return this;
    }

    public StageLog getLog() {
        return log;
    }

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public Stage putMetric(final String key, final Object value) {
        this.metrics.put(key, value);
        return this;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public ActionConfiguration getParameters() {
        return parameters;
    }

    public Stage setParameters(ActionConfiguration parameters) {
        this.parameters = parameters;

        return this;
    }

    public Executor getExecutor() {
        return job == null ? null : job.getExecutor();
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public Stage start() {
        this.executionStatus = ExecutionStatus.IN_PROGRESS;
        this.timeline.start();
        return this;
    }

    public Stage end(boolean success) {
        this.executionStatus = success ? ExecutionStatus.DONE_OK : ExecutionStatus.DONE_ERROR;
        this.timeline.end();
        return this;
    }

    public String getCondition() {
        return condition;
    }

    public Stage setCondition(String condition) {
        this.condition = condition;
        return this;
    }

    public List<StagePhaseMetaData> getPhases() {
        return phases;
    }

    public Stage addPhase(StagePhaseMetaData item) {
        this.phases.add(item);
        return this;
    }

    /**
     * @since v5.0.0
     *
     * @return labels
     */
    public Map<String, String> getLabels() {
        return labels;
    }

    public String getLabel(String key)
    {
        return labels.get(key);
    }

    /**
     * @since v5.0.0
     *
     * @param labels labels
     * @return this
     */
    public Stage setLabels(Map<String, String> labels) {
        this.labels = labels;
        return this;
    }

    @Override
    public Map<String, Object> toHash() {
        return MyMapUtils.map(
                "name", name,
                "kind", kind,
                "allow_failure", allowFailure,
                "enabled", enabled,
                "config", spec
        );
    }

    public Map<String, Object> toLog() {
        return MyMapUtils.map(
            "name", name,
            "kind", kind,
            "allow_failure", allowFailure
        );
    }
}
