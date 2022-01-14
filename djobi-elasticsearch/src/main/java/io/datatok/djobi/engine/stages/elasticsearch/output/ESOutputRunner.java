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
            if (!checkStorageIsOK(config, job)) {
                throw new StageException(stage, "[output:elasticsearch] Storage is not OK!");
            }
        }

        logger.info(String.format("[output:elasticsearch] Output Job [%s] to [%s/%s]", job.getId(), config.host, config.realIndex));

        final Map<String, String> esOptions = MyMapUtils.mapString(
            "es.nodes", config.host,
            /* "es.port", "9200", */
            "es.nodes.wan.only", "true" // @todo this should be a setting
        );

        // @todo this should be a setting
        if (config.url.startsWith("https:") || config.url.contains(":443"))
        {
            esOptions.put("es.net.ssl", "true");
            esOptions.put("es.net.ssl.cert.allow.self.signed", "true");
        }

        final String esVersion = esUtils.getElasticsearchServerVersion(config.url);
        String indexName = config.index;

        // @todo this should be a setting
        if (esVersion.startsWith("7") || esVersion.startsWith("8"))
        {
            indexName = config.realIndex; // Remove index type if ES >= 7
        }

        JavaEsSparkSQL.saveToEs(
            ((SparkDataframe) contextData).getData(),
            indexName,
            esOptions
        );

        return ActionRunResult.success();
    }

    /**
     * @since #ANA-190
     *
     * @param config
     * @param Job
     * @return bool
     * @throws Exception
     */
    private boolean checkStorageIsOK(final ESOutputConfig config, final Job Job) throws Exception {
        logger.info(String.format("[output:elasticsearch] clean: %s/%s?q=%s", config.url, config.realIndex, config.clean_query));

        int deleteTries = 1000;

        try {

            final String realEsIndex = config.realIndex;

            while(true) {
                esUtils.refresh(config.url, realEsIndex);
                // Disable flush, as it timeout with es7
                //http.post(String.format("%s/%s/_flush", config.url, realEsIndex)).execute().close();

                final HashMap<String, Object> response = http.get(
                        String.format("%s/%s/_count?q=%s", config.url, realEsIndex, URLEncoder.encode(config.clean_query, "UTF-8"))
                ).executeAsDict();

                if (response.containsKey("count")) {
                    final Object buffer = response.get("count");
                    final int itemsCount;

                    if (buffer instanceof BigDecimal) {
                        itemsCount = ((BigDecimal) buffer).intValue();
                    } else {
                        itemsCount = (int) buffer;
                    }

                    if (itemsCount == 0) {
                        return true;
                    } else {

                        deleteTries--;

                        if (deleteTries == 0) {
                            logger.error("[output:elasticsearch] clean: wait too many time > leaving, you should buy better disk!");
                            return false;
                        }

                        final String esVersion = esUtils.getElasticsearchServerVersion(config.url);

                        logger.info(String.format("[output:elasticsearch] clean: elasticsearch version [%s] storage", esVersion));

                        final HttpRequest cleanRequest;

                        if (esVersion.startsWith("2"))
                        {
                            cleanRequest = http.delete(String.format("%s/%s/_query?size=10000&timeout=10s&q=%s", config.url, config.index, URLEncoder.encode(config.clean_query, "UTF-8")));
                        }
                        else
                        {
                            cleanRequest = http.post(String.format("%s/%s/_delete_by_query?size=10000&timeout=10s&q=%s", config.url, realEsIndex, URLEncoder.encode(config.clean_query, "UTF-8")));
                        }

                        HttpResponse r = cleanRequest.execute();

                        if (r.statusCode() == 404) {
                            logger.error("delete query gives a 404 error, maybe a plugin is missing?");
                            return false;
                        }

                        logger.info(r.raw());

                        Thread.sleep(2000);
                    }
                } else if (response.containsKey("status") && response.get("status").toString().equals("404")) {
                    logger.info("[output:elasticsearch] clean: index does not exists > no cleaning");

                    return true;
                } else {
                    logger.error("[output:elasticsearch] clean: weird response when getting count > wtf?");

                    return true;
                }

                Thread.sleep(1000);
            }
        } catch(IOException e) {
            logger.error("[output:elasticsearch] clean: exception", e);

            throw e;
        }

    }

}
