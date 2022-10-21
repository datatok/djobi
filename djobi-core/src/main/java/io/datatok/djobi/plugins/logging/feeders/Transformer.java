package io.datatok.djobi.plugins.logging.feeders;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.Stage;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class Transformer {

    @Inject
    WorkflowFeeder workflowFeeder;

    @Inject
    ExecutorFeeder executorFeeder;

    @Inject
    JobFeeder jobTransformer;

    @Inject
    StageFeeder stageFeeder;

    @Inject
    DjobiFeeder djobiFeeder;

    @Inject
    APMFeeder apmFeeder;

    @Inject
    private Provider<DriverLogFeeder> driverLogResourceProvider;

    public Map<String, Object> transform(final Job job) {
        final Map<String, Object> data = new HashMap<>();

        this.jobTransformer.append(data, job);
        this.workflowFeeder.append(data, job.getWorkflow());
        this.executorFeeder.append(data, job.getExecutor());
        this.djobiFeeder.append(data);
        this.driverLogResourceProvider.get().append(data);
        this.apmFeeder.append(data, job.getMeta());

        return data;
    }

    public Map<String, Object> transform(final Stage stage) {
        final Map<String, Object> data = new HashMap<>();

        final Job job = stage.getJob();

        this.stageFeeder.append(data, stage);
        this.jobTransformer.append(data, job);
        this.workflowFeeder.append(data, job.getWorkflow());
        this.executorFeeder.append(data, job.getExecutor());
        this.djobiFeeder.append(data);
        this.driverLogResourceProvider.get().append(data);
        this.apmFeeder.append(data, job.getMeta());

        return data;
    }

}
