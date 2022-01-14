package io.datatok.djobi.spark.actions.mutate;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.exceptions.StageException;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.actions.DataFrameBean;
import io.datatok.djobi.spark.data.SparkDataframe;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.functions;
import org.apache.spark.util.LongAccumulator;

import java.util.Map;

public class SparkMutateRunner implements ActionRunner {

    private static Logger logger = Logger.getLogger(SparkMutateRunner.class);

    @SuppressWarnings("unchecked")
    public ActionRunResult run(final Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        final Job job = stage.getJob();
        final SparkMutateConfig config = (SparkMutateConfig) stage.getParameters();
        final JavaSparkContext javaSparkContext = (JavaSparkContext) job.getPipeline().getExecutor().get("java_context");

        if (config.dataframe_class != null && !config.dataframe_class.isEmpty()) {

            final Class<DataFrameBean> clazz = (Class<DataFrameBean>) Class.forName(config.dataframe_class);

            final LongAccumulator counterTotal  = javaSparkContext.sc().longAccumulator("mutate_total");
            final LongAccumulator counterSuccess  = javaSparkContext.sc().longAccumulator("mutate_success");
            final LongAccumulator counterErrors  = javaSparkContext.sc().longAccumulator("mutate_errors");

            if (contextData.getData() instanceof JavaRDD) {
                final SQLContext sqlContext = (SQLContext) job.getPipeline().getExecutor().get("sql_context");
                final JavaRDD<DataFrameBean> rdds = ((JavaRDD<Object>) contextData.getData()).map(rddData -> {
                    final DataFrameBean bean = clazz.newInstance();

                    try {
                        bean.transform(rddData);
                    } catch(Exception e) {
                        logger.error("Transform RDD", e);
                        counterErrors.add(1);
                    } finally {
                        counterSuccess.add(1);
                    }

                    counterTotal.add(1);

                    return bean;
                });

                final Dataset<Row> df = sqlContext.createDataFrame(rdds, clazz);

                return ActionRunResult.success(new SparkDataframe(df));
            }
        }

        if (contextData instanceof SparkDataframe) {
            Dataset<Row> data = ((SparkDataframe) contextData).getData();

            if (config.sortColumn != null) {
                if (config.sortDirection == null || config.sortDirection.equals("asc")) {
                    data = data.sort(functions.asc(config.sortColumn));
                } else {
                    data = data.sort(functions.desc(config.sortColumn));
                }
            }

            if (config.repartition_count > 0) {
                if (config.repartition_column != null && !config.repartition_column.isEmpty()) {
                    data = data.repartition(config.repartition_count, new Column(config.repartition_column));
                } else {
                    data = data.coalesce(config.repartition_count);
                }
            }

            if (config.renames != null) {
                for (Map.Entry<String, String> rename : config.renames.entrySet()) {
                    logger.debug(String.format("Rename %s to %s ", rename.getKey(), rename.getValue()));

                    data = data.withColumnRenamed(rename.getKey(), rename.getValue());
                }
            }

            if (config.adds != null) {
                for (Map.Entry<String, String> rename : config.adds.entrySet()) {
                    logger.debug(String.format("Add column %s to %s ", rename.getKey(), rename.getValue()));

                    data = data.withColumn(rename.getKey(), functions.lit(rename.getValue()));
                }
            }

            if (config.deletes != null) {
                for (String field : config.deletes) {
                    logger.debug(String.format("Remove column %s", field));
                    data = data.drop(field);
                }
            }

            if (config.limit > 0) {
                logger.debug(String.format("Apply limit of %d", config.limit));
                data = data.limit(config.limit);
            }

            if (config.broadcast) {
                javaSparkContext.broadcast(data);
            }

            if (config.cast != null && config.cast.size() > 0) {
                for (Map.Entry<String, String> entry : config.cast.entrySet()) {
                    logger.debug(String.format("Cast column %s to %s ", entry.getKey(), entry.getValue()));

                    data = data.withColumn(entry.getKey(), data.col(entry.getKey()).cast(entry.getValue()));
                }
            }

            if (config.alias_table != null && !config.alias_table.isEmpty()) {
                logger.debug(String.format("Alias as table %s", config.alias_table));
                data.createOrReplaceTempView(config.alias_table);
            }

            return ActionRunResult.success(new SparkDataframe(data));

        } else {
            throw new StageException(stage, "data must be DataFrame!");
        }
    }
}
