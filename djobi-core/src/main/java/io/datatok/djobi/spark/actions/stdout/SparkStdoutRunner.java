package io.datatok.djobi.spark.actions.stdout;

import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.data.SparkDataframe;

/**
 * Implementation of "stdout" for Spark executor.
 */
public class SparkStdoutRunner implements ActionRunner {
    @Override
    public ActionRunResult run(Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        if (contextData instanceof SparkDataframe)
        {
            ((SparkDataframe)contextData).getData().limit(100).show();

            return ActionRunResult.success();
        }

        return ActionRunResult.fail("wrong data type!");
    }
}
