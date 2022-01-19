package io.datatok.djobi.test;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.ActionFactory;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionConfigurator;
import io.datatok.djobi.engine.stage.livecycle.ActionPostChecker;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.executors.ExecutorPool;
import io.datatok.djobi.utils.Bag;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TestEngine {

    @Inject
    private ActionFactory actionFactory;

    @Inject
    private ExecutorPool executorPool;

    private Stage stage = StageTestUtils.getNewStage();

    public TestEngine newStage() {
        stage = StageTestUtils.getNewStage();
        return this;
    }

    public Stage getStage() {
        return this.stage;
    }

    public Job getJob() {
        return this.stage.getJob();
    }

    public TestEngine withKind(final String type) {
        stage.setKind(type);

        return this;
    }

    public TestEngine withSpec(final Bag spec) {
        stage.setSpec(spec);

        return this;
    }

    public TestEngine withJobData(final StageData<?> data) {
        getJob().setData(data);

        return this;
    }

    public ActionRunner getRunner() {
        return actionFactory.getRunner(stage);
    }

    public TestEngine run() throws Exception {
        if (stage.getJob().getExecutor() != null) {
            stage.getJob().getExecutor().connect();
        }

        getRunner().run(stage, stage.getJob().getData(), stage.getJob().getExecutor());

        return this;
    }

    public ActionPostChecker getPostChecker() {
        return actionFactory.getPostChecker(stage);
    }


    public CheckResult postCheck() throws Exception {
        if (stage.getJob().getExecutor() != null) {
            stage.getJob().getExecutor().connect();
        }

        return getPostChecker().postCheck(stage);
    }

    public TestEngine configure() {
        ActionConfigurator configurator = actionFactory.getConfigurator(stage);

        if (configurator != null) {
            stage.setParameters(configurator.configure(stage.getJob(), stage));
        }

        return this;
    }

}
