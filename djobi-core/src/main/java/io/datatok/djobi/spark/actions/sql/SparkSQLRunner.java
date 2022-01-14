package io.datatok.djobi.spark.actions.sql;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.actions.sql.SQLConfig;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import javax.inject.Inject;
import java.io.IOException;

public class SparkSQLRunner implements ActionRunner {

    private static final Logger logger = Logger.getLogger(SparkSQLRunner.class);

    @Inject
    protected TemplateUtils templateUtils;

    @Override
    public ActionRunResult run(final Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        final Job job = stage.getJob();
        final SQLConfig configuration = (SQLConfig) stage.getParameters();

        final String argQuery = configuration.query; // @todo remove DOUBLE RENDER ?? templateUtils.render(configuration.query, job);
        final SQLContext sqlContext = (SQLContext) job.getPipeline().getExecutor().get("sql_context");

        final String sqlQuery = getQuery(argQuery, job);

        final String[] queries = sqlQuery.split("---EOS---");

        Dataset<Row> dataFromSQL = null;

        for (int i = 0; i < queries.length; i++) {
            queries[i] = queries[i].trim();

            logger.info("\n" + queries[i]);

            if (queries.length == 1 || isSelectableQuery(queries[i])) {
                dataFromSQL = sqlContext.sql(queries[i]);
            } else {
                sqlContext.sql(queries[i]);
            }
        }

        if (dataFromSQL == null)
        {
            throw new Exception("Didnt find a selectable query!");
        }

        return ActionRunResult.success(new SparkDataframe(dataFromSQL));
    }

    private String getQuery(final String queryOrPath, final Job job) throws IOException {
        if (queryOrPath.endsWith(".sql")) {
            final String sqlQuery = job.getPipeline().getResources(queryOrPath);

            return templateUtils.render(sqlQuery, job);
        } else {
            return queryOrPath;
        }
    }

    private boolean isSelectableQuery(final String query)
    {
        return query.toLowerCase().trim().startsWith("select") || query.toLowerCase().trim().startsWith("--select") || query.toLowerCase().trim().startsWith("-- select");
    }
}
