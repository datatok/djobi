package io.datatok.djobi.plugins.s3.actions;

import io.datatok.djobi.engine.actions.fs.output.FSOutputConfig;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.exceptions.StageException;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.plugins.s3.S3SparkUtils;
import io.datatok.djobi.spark.actions.fs.SparkFSOutput;
import io.datatok.djobi.spark.data.SparkDataKind;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.spark.executor.SparkExecutor;
import org.apache.log4j.Logger;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import javax.inject.Inject;

public class FSOutputS3Spark extends SparkFSOutput implements ActionRunner {
    @Inject
    private SparkExecutor sparkExecutor;

    private static final Logger logger = Logger.getLogger(FSOutputS3Spark.class);

    @Override
    public ActionRunResult run(Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        final SparkContext sc = sparkExecutor.getSparkContext();
        final StageData<?> data = stage.getJob().getData();

        if (!SparkDataKind.isSparkData(data)) {
            throw new StageException(stage, "data format not supported!");
        }

        if (!(data instanceof SparkDataframe)) {
            throw new StageException(stage, "data format not supported!");
        }

        this.config = (FSOutputConfig) stage.getParameters();

        final Dataset<Row> df = ((SparkDataframe) data).getData();

        S3SparkUtils.configure(sc.hadoopConfiguration(), config.s3);

        logger.info("Writing DataFrame via S3");

        this.writeToFS(df);

        return ActionRunResult.success();
    }
}
