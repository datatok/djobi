package io.datatok.djobi.spark.actions.transformers;

import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.data.SparkData;
import io.datatok.djobi.spark.data.SparkDataKind;
import io.datatok.djobi.spark.data.SparkDataframe;
import org.apache.spark.sql.Dataset;

public class SparkJSONRunner implements ActionRunner {

    @Override
    public ActionRunResult run(Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        if (contextData instanceof SparkDataframe) {
            Dataset<String> lines = ((SparkDataframe) contextData).getData().toJSON();

            return ActionRunResult.success(new SparkData<>(SparkDataKind.TYPE_DATASET, lines));
        }

        throw new Exception("data must be a Spark DataFrame!");
    }
}
