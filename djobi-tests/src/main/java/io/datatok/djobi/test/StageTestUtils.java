package io.datatok.djobi.test;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.flow.ExecutionContext;
import io.datatok.djobi.engine.stage.ActionFactory;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionConfigurator;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.executors.ExecutorPool;
import io.datatok.djobi.utils.Bag;

@Singleton
public class StageTestUtils {

    @Inject
    private ActionFactory actionFactory;

    @Inject
    private ExecutorPool executorPool;

    @Inject
    private ExecutionContext executionContext;

    public StageRunBuilder builder() {
        return new StageRunBuilder(actionFactory, executorPool, executionContext);
    }

    public Stage configure(Stage stage) {
        ActionConfigurator configurator = actionFactory.getConfigurator(stage);

        if (configurator != null) {
            stage.setParameters(configurator.configure(stage.getJob(), stage));
        }

        return stage;
    }

    public void run(String type) throws Exception {
        run(type, new Bag());
    }

    public Job run(String type, Bag config) throws Exception {
        return run(type, config, new Job());
    }

    public Job run(Stage stage, Job job) throws Exception {
        configure(stage);

        ActionRunner runner = actionFactory.getRunner(stage);

        if (runner == null) {
            throw new Exception(String.format("stage runner for %s is null!", stage.getKind()));
        }

        runner.run(stage, job.getData(), stage.getJob().getExecutor());

        return job;
    }

    public ActionRunResult run(Stage stage, StageData<?> contextData) throws Exception {
        configure(stage);

        ActionRunner runner = actionFactory.getRunner(stage);

        if (runner == null) {
            throw new Exception(String.format("stage runner for %s is null!", stage.getKind()));
        }

        return runner.run(stage, contextData, stage.getJob().getExecutor());
    }

    public Job run(String type, Bag config, Job job) throws Exception {
        final Stage stage = getNewStage().setJob(job);

        stage
            .setSpec(config)
            .setKind(type)
        ;

        return run(stage, job);
    }

    static public Stage getNewStage() {
        Stage stage = new Stage();
        Job job = new Job();
        Pipeline pipeline = new Pipeline();

        job.setPipeline(pipeline);
        stage.setJob(job);

        return stage;
    }

    static public Stage getNewStage(String kind, Bag spec) {
        Stage stage = new Stage();
        Job job = new Job();
        Pipeline pipeline = new Pipeline();

        job.setPipeline(pipeline);

        return stage
            .setJob(job)
            .setKind(kind)
            .setSpec(spec)
        ;
    }
}
