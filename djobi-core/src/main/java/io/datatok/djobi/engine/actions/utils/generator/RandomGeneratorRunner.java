package io.datatok.djobi.engine.actions.utils.generator;

import com.google.inject.Inject;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.exceptions.StageException;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.executors.ExecutorPool;

public class RandomGeneratorRunner implements ActionRunner {

    private RandomGeneratorConfig config;

    @Inject
    private ExecutorPool executorPool;

    @Override
    public ActionRunResult run(Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        throw new StageException(stage, "Not supported, use with Spark executor only!");
    }
}
