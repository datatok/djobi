package io.datatok.djobi.user_agent;

import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.exceptions.StageException;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.user_agent.parsers.BitwalkerParser;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.functions;

public class UserAgentParserRunner implements ActionRunner {

    @Override
    public ActionRunResult run(Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        if (contextData == null) {
            throw new StageException(stage, "data is null!");
        }

        if (!(contextData instanceof SparkDataframe)) {
            throw new StageException(stage, "data must be a Spark dataset!");
        }

        final UserAgentParserConfig config = (UserAgentParserConfig) stage.getParameters();

        final Dataset<Row> df = ((SparkDataframe) contextData).getData();
        final SQLContext sqlContext = (SQLContext) stage.getJob().getPipeline().getExecutor().get("sql_context");

        if (sqlContext == null) {
            throw new StageException(stage, "Cannot get sql context from executor!");
        }

        new BitwalkerParser().setup(sqlContext, config.fields);

        String column = config.fieldTarget;
        Column columnValue = functions.callUDF("parse_user_agent", functions.col(config.fieldSource));

        if (column.contains(".")) {
            throw new StageException(stage, "Dont support nested path yet!");
            /*
            String[] buffer = column.split("\\.");

            if (buffer.length > 2) {
                throwException("Too nested path! We support only column.struct path.");
            }

            column = buffer[0];
            columnValue = functions.(df.col(column), columnValue).as(buffer[1]);*/
        }

        final Dataset<Row> newDf = df.withColumn(column, columnValue);

        return ActionRunResult.success(new SparkDataframe(newDf));
    }
}
