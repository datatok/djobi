package io.datatok.djobi.engine.actions.transformer.serializer;

import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.data.StageDataListString;
import io.datatok.djobi.engine.data.StageDataString;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.exceptions.DataException;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.data.SparkDataframe;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;

public class SerializerRunner implements ActionRunner {

    private static Logger logger = Logger.getLogger(SerializerRunner.class);

    @Override
    public ActionRunResult run(Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        final SerializerConfig config = (SerializerConfig) stage.getParameters();

        logger.debug(String.format("[config] %s", config.toString()));

        // @todo move to Spark package
        if (contextData instanceof SparkDataframe) {
            final Dataset<Row> df = ((SparkDataframe) contextData).getData();

            List<String> lines = df.toJSON().collectAsList();

            return ActionRunResult.success(new StageDataListString(lines));
            //job.setData(String.join("\n", (String[]) ));
        }

        if (contextData instanceof StageDataListString) {
            List<String> lines = ((StageDataListString) contextData).getData();
            return ActionRunResult.success(new StageDataString(String.join("\n", lines)));
        }


        throw new DataException(stage, "data must be a Spark DataFrame!");
    }
}
