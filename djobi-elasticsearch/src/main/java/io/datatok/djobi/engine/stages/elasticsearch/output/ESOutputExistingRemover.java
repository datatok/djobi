package io.datatok.djobi.engine.stages.elasticsearch.output;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.utils.elasticsearch.ElasticsearchUtils;
import io.datatok.djobi.utils.http.Http;
import io.datatok.djobi.utils.http.HttpRequest;
import io.datatok.djobi.utils.http.HttpResponse;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;

@Singleton
public class ESOutputExistingRemover {

    @Inject
    private Http http;

    @Inject
    private ElasticsearchUtils esUtils;

    private static final Logger logger = Logger.getLogger(ESOutputExistingRemover.class);

    private int getCountValue(HashMap<String, Object> response) {
        if (response.containsKey("count")) {
            final Object buffer = response.get("count");

            if (buffer instanceof BigDecimal) {
                return ((BigDecimal) buffer).intValue();
            } else {
                return (int) buffer;
            }
        }

        return -1;
    }

    /**
     * @since #ANA-190
     *
     * @param config
     * @param Job
     * @return bool
     * @throws Exception
     */
    public boolean execute(final ESOutputConfig config, final Job Job) throws Exception {
        logger.info(String.format("[output:elasticsearch] clean: %s/%s?q=%s", config.url, config.index, config.clean_query));

        int deleteTries = 1000;

        try {

            final String realEsIndex = config.index;

            while(true) {
                esUtils.refresh(config.url, realEsIndex);
                // Disable flush, as it timeout with es7
                //http.post(String.format("%s/%s/_flush", config.url, realEsIndex)).execute().close();

                final HashMap<String, Object> response = http.get(
                        String.format("%s/%s/_count?q=%s", config.url, realEsIndex, URLEncoder.encode(config.clean_query, "UTF-8"))
                ).executeAsDict();

                final int itemsCount = getCountValue(response);

                if (itemsCount == - 1) {
                    if (response.containsKey("status") && response.get("status").toString().equals("404")) {
                        logger.info("[output:elasticsearch] clean: index does not exists > nothing to delete");
                    } else {
                        logger.error("[output:elasticsearch] clean: weird response when getting count > wtf?");
                    }
                    return true;
                } else if (itemsCount == 0) {
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
            }
        } catch(IOException e) {
            logger.error("[output:elasticsearch] clean: exception", e);

            throw e;
        }
    }
}
