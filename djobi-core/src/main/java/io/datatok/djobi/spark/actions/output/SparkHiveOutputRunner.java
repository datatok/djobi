package io.datatok.djobi.spark.actions.output;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.data.SparkDataKind;
import io.datatok.djobi.spark.data.SparkDataframe;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import scala.collection.JavaConversions;

public class SparkHiveOutputRunner implements ActionRunner {

    private Stage stage;

    private static Logger logger = Logger.getLogger(SparkHiveOutputRunner.class);

    @Override
    public ActionRunResult run(final Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        final Job job = stage.getJob();

        this.stage = stage;

        if (contextData == null || contextData.getData() == null) {
            throw new Exception("[output:hive] data is null!");
        }

        if (!SparkDataKind.isSparkData(contextData)) {
            throw new Exception("[output:hive] only support Spark data!");
        }

        SparkDataframe sparkDataframe = (SparkDataframe) contextData.getData();

        final SparkHiveOutputConfig config = (SparkHiveOutputConfig) stage.getParameters();

        logger.info(String.format("[output:hive] Output Job [%s] to [%s]", job.getId(), config.table));

        DataFrameWriter<Row> writer = sparkDataframe.getData()
                .repartition(config.num_partitions)
                .write();

        if (config.partitions != null) {
            writer = writer.partitionBy(JavaConversions.asScalaBuffer(config.partitions).toSeq()); /** @todo Get partition from table if any */
        }

        writer.insertInto(config.table);

        Thread.sleep(1000);

        final SQLContext sqlEngine = (SQLContext) stage.getExecutor().get("sql_context");

        sqlEngine.sql("REFRESH TABLE " + config.table);

        Thread.sleep(1000);

        if (config.path != null && config.path.length() > 0) {
            cleanStagingFiles(config.path);
        }

        return ActionRunResult.success();
    }

    /**
     * Remove staging files
     * @param path
     * @throws Exception
     */
    private void cleanStagingFiles(final String path) throws Exception {
        final FileSystem fs = (FileSystem) stage.getExecutor().get("hdfs");
        final FileStatus[] filesStatus = fs.listStatus(new Path(path));

        for (FileStatus fileStatus : filesStatus) {
            final String filePathName = fileStatus.getPath().toString();

            if (filePathName.contains(".hive-staging")) {
                logger.info(String.format("[output:hive] Remove [%s]", filePathName));
                // Secure :p
                if (fileStatus.getPath().toString().length() > 30) {
                    fs.delete(fileStatus.getPath(), true);
                } else {
                    logger.error("path not sure to delete automatically files!");
                }
            }
        }
    }

}
