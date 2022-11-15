package io.datatok.djobi.spark.actions.fs;

import io.datatok.djobi.engine.actions.fs.input.FSInputConfig;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.exceptions.NotFoundException;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.spark.executor.SparkExecutor;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import javax.inject.Inject;

public class SparkFSInput implements ActionRunner {

    @Inject
    private SparkExecutor sparkExecutor;

    @Override
    public ActionRunResult run(Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        final FSInputConfig config = (FSInputConfig) stage.getParameters();
        final FileSystem hdfs = sparkExecutor.getHDFS();
        final SQLContext sqlContext = sparkExecutor.getSQLContext();

        // Skip test
        /*if ((config.paths == null || config.paths.isEmpty()) && !hdfs.exists(new Path(config.path))) {
            throw new NotFoundException(stage, "input file " + config.path + " not found!");
        }*/

        final Dataset<Row> df = getDataset(sqlContext, config.path, config.paths, config.format);

        return ActionRunResult.success(new SparkDataframe(df));
    }

    private Dataset<Row> getDataset(final SQLContext sqlContext, final String path, final String paths, final String format) {
        final DataFrameReader dfReader = sqlContext.read().format(format);

        if (paths.isEmpty()) {
            return dfReader.load(path);
        } else {
            return dfReader.option("paths", paths).load();
        }
    }
}
