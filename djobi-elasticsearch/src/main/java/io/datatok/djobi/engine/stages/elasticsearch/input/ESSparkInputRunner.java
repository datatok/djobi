package io.datatok.djobi.engine.stages.elasticsearch.input;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.data.SparkData;
import io.datatok.djobi.spark.data.SparkDataKind;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.utils.MyMapUtils;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.elasticsearch.spark.rdd.api.java.JavaEsSpark;

import java.util.Map;

public class ESSparkInputRunner implements ActionRunner {

    private static Logger logger = Logger.getLogger(ESSparkInputRunner.class);

    @Override
    public ActionRunResult run(Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        final Job job = stage.getJob();
        final ESSparkInputConfig config = (ESSparkInputConfig) stage.getParameters();

        final Executor executor = job.getWorkflow().getExecutor();

        final Map<String, String> options = MyMapUtils.map(
                "es.nodes", config.url,
                "es.resource", config.index
        );

        if (config.query != null && !config.query.isEmpty()) {
            if (config.query.startsWith("file://")) {
                final String buffer = job.getWorkflow().getResources(config.query.replace("file://", ""));

                options.put("es.query", buffer);
            } else {
                options.put("es.query", "?q=" + config.query);
            }
        }

        if (config.esOptions != null) {
            options.putAll(config.esOptions);
        }

        logger.debug(String.format("Read elasticsearch %s/%s as %s", config.url, config.index, config.data_type));
        logger.debug(options);

        switch(config.data_type) {
            case "rdd":
                JavaRDD<Map<String, Object>> rdd = createRDD((JavaSparkContext) executor.get("java_context"), options);

                return ActionRunResult.success(new SparkData<>(SparkDataKind.TYPE_JAVA_RDD, rdd));
            case "dataframe":
            case "dataset":
                Dataset<Row> df = createDataset((SQLContext) executor.get("sql_context"), options);

                return ActionRunResult.success(new SparkDataframe(df));
            case "table":
                createDataset((SQLContext) executor.get("sql_context"), options).createOrReplaceTempView(config.table);

                return ActionRunResult.success();
        }

        return ActionRunResult.fail("??");
    }

    private JavaRDD<Map<String, Object>> createRDD(final JavaSparkContext sparkContext, final Map<String, String> options) {
        return JavaEsSpark.esRDD(sparkContext, options).values();
    }

    private Dataset<Row> createDataset(final SQLContext sqlContext, final Map<String, String> options) {
        return sqlContext.read()
                .format("org.elasticsearch.spark.sql")
                .options(options)
                .load()
        ;
    }

}
