package io.datatok.djobi.spark.actions.fs;

import io.datatok.djobi.engine.actions.fs.output.FSOutputConfig;
import io.datatok.djobi.engine.actions.fs.output.FSOutputRunner;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.exceptions.StageException;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.data.SparkDataKind;
import io.datatok.djobi.spark.data.SparkDataframe;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;

public class SparkFSOutput implements ActionRunner {

    protected FSOutputConfig config;

    private static final Logger logger = Logger.getLogger(FSOutputRunner.class);

    @Override
    public ActionRunResult run(Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        if (!SparkDataKind.isSparkData(contextData)) {
            throw new StageException(stage, "data format not supported!");
        }

        if (!(contextData.getKind().getType().equals(SparkDataKind.TYPE_DATASET) )) {
            throw new StageException(stage, "data must be a dataset!");
        }

        this.config = (FSOutputConfig) stage.getParameters();

        if (config.path.startsWith("s3a://")) {
            throw new Exception("S3 is supported via S3 plugin!");
        } else {
            writeToFS(((SparkDataframe) contextData).getData());
        }

        return ActionRunResult.success();
    }

    protected void writeToFS(final Dataset<?> df) {
        logger.info(String.format("Writing DataFrame as %s at %s", config.format, config.path));

        String mode = "error";

        if (config.mode != null && !config.mode.isEmpty())
        {
            mode = config.mode;
        }

        df
            .write()
            .mode(mode)
            .options(config.options)
            .format(config.format)
            .save(config.path)
        ;
    }

}
