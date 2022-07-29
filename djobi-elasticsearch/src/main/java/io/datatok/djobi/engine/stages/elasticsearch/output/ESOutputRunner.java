package io.datatok.djobi.engine.stages.elasticsearch.output;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.exceptions.StageException;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.utils.MyMapUtils;
import io.datatok.djobi.utils.elasticsearch.ElasticsearchUtils;
import io.datatok.djobi.utils.http.Http;
import io.datatok.djobi.utils.http.HttpRequest;
import io.datatok.djobi.utils.http.HttpResponse;
import org.apache.log4j.Logger;
import org.elasticsearch.spark.sql.api.java.JavaEsSparkSQL;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Elasticsearch output via Spark.
 * <!> Support only DataFrame </!>
 */
public class ESOutputRunner implements ActionRunner {

    private static final Logger logger = Logger.getLogger(ESOutputRunner.class);

    @Inject
    private Http http;

    @Inject
    private ElasticsearchUtils esUtils;

    @Inject
    private ESOutputExistingRemover existingRemover;

    @Override
    public ActionRunResult run(final Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        final Job job = stage.getJob();
        final ESOutputConfig config = (ESOutputConfig) stage.getParameters();

        org.elasticsearch.hadoop.util.Version.logVersion();

        if (config == null) {
            throw new StageException(stage, "config is null!");
        }

        if (contextData == null) {
            throw new StageException(stage, "data is null!");
        }

        if (!(contextData instanceof SparkDataframe)) {
            throw new StageException(stage, "data type not supported!");
        }

        //System.out.println(((DataFrame) job.getData()).count());

        if (config.clean_query != null && !config.clean_query.isEmpty()) {
            if (!existingRemover.execute(config, job)) {
                throw new StageException(stage, "[output:elasticsearch] Storage is not OK!");
            }
        }

        logger.info(String.format("[output:elasticsearch] Output Job [%s] to [%s/%s]", job.getId(), config.host, config.index));

        final Map<String, String> esOptions = MyMapUtils.mapString(
            "es.nodes", config.host
        );

        JavaEsSparkSQL.saveToEs(
            ((SparkDataframe) contextData).getData(),
            config.index,
            esOptions
        );

        return ActionRunResult.success();
    }
}
