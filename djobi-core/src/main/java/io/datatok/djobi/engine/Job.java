package io.datatok.djobi.engine;

import io.datatok.djobi.engine.check.CheckStatus;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.enums.ExecutionStatus;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.utils.Dumpable;
import io.datatok.djobi.utils.MyMapUtils;
import io.datatok.djobi.utils.Timeline;
import io.datatok.djobi.utils.bags.ParameterBag;
import io.datatok.djobi.utils.interfaces.Labelized;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Job implements Dumpable, Labelized {

    static public final String META_APM_TRACE_ID = "apm_trace_id";

    private List<Stage> stages;

    private ParameterBag parameters;

    private Workflow workflow;

    private String id;

    private String name;

    private final String uid;

    private int order;

    /**
     * @deprecated Use ContextExecution.getData()
     */
    private StageData<?> data;

    private String contextKey;

    private Map<String, Object> meta = new HashMap<>();

    /**
     * @since v1.2.0
     *
     * If job run have been executed or skipped.
     */
    private boolean executed = false;

    /**
     * @since v2.2.1
     */
    private ExecutionStatus executionStatus = ExecutionStatus.NO;

    /**
     * @since v2.2.1
     */
    final private Timeline timeline = new Timeline();

    /**
     * @since v1.6.2 (rename at v2.2.11)
     */
    private CheckStatus preCheckStatus = CheckStatus.NO;

    /**
     * @since v2.2.11
     */
    private CheckStatus postCheckStatus = CheckStatus.TODO;

    /**
     * @since v5.0.0
     */
    public Map<String, String> labels;


    public Job() {
        this.uid = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public Job setId(String id) {
        this.id = id;

        return this;
    }

    public String getUid() {
        return uid;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public Job setWorkflow(Workflow workflowDefinition) {
        this.workflow = workflowDefinition;

        return this;
    }

    public ParameterBag getParameters() {
        return parameters;
    }

    public Job setParameters(ParameterBag parameters) {
        this.parameters = parameters;

        return this;
    }

    public List<Stage> getStages() {
        return stages;
    }

    public Job setStages(List<Stage> stages) {
        this.stages = stages;

       // stages.forEach((k, v) -> v.setName(k));

        return this;
    }

    public int getOrder() {
        return order;
    }

    public Job setOrder(int order) {
        this.order = order;

        return this;
    }

    /**
     * @deprecated Use ContextExecution.getData()
     */
    public StageData<?> getData() {
        return data;
    }

    /**
     * @deprecated Use ContextExecution.getData()
     */
    public Job setData(StageData<?> data) {
        this.data = data;

        return this;
    }

    public String getContextKey() {
        return contextKey;
    }

    public Job setContextKey(String contextKey) {
        this.contextKey = contextKey;

        return this;
    }

    public boolean isExecuted() {
        return executed;
    }

    public Job setExecuted(boolean executed) {
        this.executed = executed;

        return this;
    }

    public String getName() {
        return name;
    }

    public Job setName(String name) {
        this.name = name;

        return this;
    }

    public CheckStatus getPreCheckStatus() {
        return preCheckStatus;
    }

    public Job setPreCheckStatus(CheckStatus preCheckStatus) {
        this.preCheckStatus = preCheckStatus;

        return this;
    }

    public CheckStatus getPostCheckStatus() {
        return postCheckStatus;
    }

    public Job setPostCheckStatus(CheckStatus postCheckStatus) {
        this.postCheckStatus = postCheckStatus;
        return this;
    }

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public Job setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;

        return this;
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public Map<String, Object> toLog() {
        return MyMapUtils.map(
            "name", name,
                "id", id,
                "order", order,
                "args", parameters
        );
    }

    @Override
    public Map<String, Object> toHash() {
        return MyMapUtils.map(
            "name", name,
                "id", id,
                "order", order,
                "args", parameters,
                "stages", stages.stream().map(Stage::toHash).collect(Collectors.toList())
        );
    }

    public void start() {
        this.executionStatus = ExecutionStatus.IN_PROGRESS;
        this.timeline.start();
    }

    public void end(boolean success) {
        this.executionStatus = success ? ExecutionStatus.DONE_OK : ExecutionStatus.DONE_ERROR;
        this.timeline.end();
    }

    public Job setMeta(final String k, final Object v) {
        this.meta.put(k, v);
        return this;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public String getLabel(String key)
    {
        return labels.get(key);
    }

    public Job setLabels(Map<String, String> labels) {
        this.labels = labels;
        return this;
    }

    /**
     * @since v3.16.0
     * @return Executor
     */
    public Executor getExecutor() {
        return this.workflow != null ? this.workflow.getExecutor() : null;
    }
}
