package io.datatok.djobi.test;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.flow.ExecutionContext;
import io.datatok.djobi.engine.stage.ActionFactory;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionConfigurator;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.executors.ExecutorPool;
import io.datatok.djobi.utils.Bag;

public class StageRunBuilder {

    private Stage stage;

    private ActionFactory actionFactory;

    private ExecutorPool executorPool;

    private ExecutionContext executionContext;

    public StageRunBuilder(ActionFactory actionFactory, ExecutorPool executorPool, ExecutionContext executionContext) {
        this.actionFactory = actionFactory;
        this.executorPool = executorPool;
        this.executionContext = executionContext;

        stage = StageTestUtils.getNewStage();
    }


    public StageRunBuilder addStageType(final String type) {
        stage.setKind(type);

        return this;
    }

    public StageRunBuilder configure(final Bag config) {
        stage.setSpec(config);

        return this;
    }

    public Stage getStage() {
        return stage;
    }

    public StageRunBuilder withData(StageData<?> data) {
        executionContext.setStageData(data);

        return this;
    }

    public StageRunBuilder attachJob(final Job job) {
        stage.setJob(job);

        return this;
    }

    public StageRunBuilder attachPipeline(final Pipeline pipeline) {
        if (stage.getJob() == null) {
            attachJob(new Job());
        }
        stage.getJob().setPipeline(pipeline);

        return this;
    }

    public StageRunBuilder attachExecutor(final Executor executor) {
        if (stage.getJob() == null || stage.getJob().getPipeline() == null) {
            attachPipeline(new Pipeline());
        }
        stage.getJob().getPipeline().setExecutor(executor);

        executionContext.setExecutor(executor);

        return this;
    }

    public StageRunBuilder attachExecutor(final String type) {
        return attachExecutor(executorPool.get(type));
    }

    public Job configure() {
        ActionConfigurator configurator = actionFactory.getConfigurator(stage);

        if (configurator != null) {
            stage.setParameters(configurator.configure(stage.getJob(), stage));
        }

        return stage.getJob();
    }

    public ActionRunResult run() throws Exception {
        StageData<?> data = null;

        if (executionContext.getData() != null)
        {
            data = executionContext.getData();
        }
        else if (stage.getJob().getData() != null)
        {
            data = stage.getJob().getData();
        }

        return run(data);
    }

    public ActionRunResult run(StageData<?> data) throws Exception {
        configure();

        ActionRunner runner = actionFactory.getRunner(stage);

        if (runner == null) {
            throw new Exception(String.format("stage runner for %s is null!", stage.getKind()));
        }

        if (stage.getJob().getExecutor() != null) {
            stage.getJob().getExecutor().connect();
        }

        return runner.run(stage, data, stage.getJob().getExecutor());
    }


}
