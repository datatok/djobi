package io.datatok.djobi.plugins.logging.sink.elasticsearch;

import com.google.inject.Inject;
import io.datatok.djobi.plugins.logging.config.LoggingSinkConfig;
import io.datatok.djobi.plugins.logging.sink.LogSink;
import io.datatok.djobi.utils.MyMapUtils;
import io.datatok.djobi.utils.http.Http;
import io.datatok.djobi.utils.http.HttpRequest;
import io.datatok.djobi.utils.http.HttpResponse;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Write work logs to elasticsearch.
 */
public class ElasticsearchLogSink implements LogSink {

    @Inject
    private Http http;
    
    static private final Logger logger = Logger.getLogger(ElasticsearchLogSink.class);

    private final String elasticsearchEndpoint;

    private final String elasticsearchIndex;

    /**
     * Hold ready status, to prevent error logs flood.
     */
    private boolean isWorking = true;

    public ElasticsearchLogSink(final LoggingSinkConfig config) {
        this.elasticsearchIndex = config.getStoreBucket();
        this.elasticsearchEndpoint = config.getStoreURL();
    }

    /**
     * Check index name contains type or not, according ES version.
     *
     * @since v3.7.0
     */
    public Map<String, Object> getDocument(String documentId) throws IOException {
        return http.get(String.format("%s/%s/%s",this.elasticsearchEndpoint, this.elasticsearchIndex, documentId)).executeAsDict();
    }

    @Override
    public String updateOrCreate(final String documentId, final Map<String, Object> data) throws Exception   {

        if (!isWorking)
        {
            return null;
        }

        HttpRequest request;

        if (documentId == null || documentId.isEmpty()) {
            final String url = String.format("%s/%s/_doc/?refresh",this.elasticsearchEndpoint, this.elasticsearchIndex);
            request = http.post(url);
        } else {
            final String url = String.format("%s/%s/_doc/%s?refresh",this.elasticsearchEndpoint, this.elasticsearchIndex, documentId);
            request = http.put(url);
        }

        data.put("@timestamp", Calendar.getInstance().getTime());

        logger.debug(String.format("index %s: %s", request.getMethod(), request.getUrl()));

        try
        {
            final HttpResponse response = request.setQuiet(true).setDataAsJSON(data).execute();

            if (response.statusCode() >= 400) {
                throw new Exception(String.format("elasticsearch sink log error: %d (%s)", request.getResponse().statusCode(), request.getResponse().raw()));
            }

            return (String) response.at("_id", true);
        }
        catch (ConnectException e)
        {
            isWorking = false;

            throw e;
        }
    }

    @Override
    public String updateOrCreate(final Map<String, Object> data) throws Exception   {
        if (!isWorking)
        {
            return null;
        }

        return updateOrCreate(null, data);
    }

    @Override
    public void updateAtomic(final String documentId, final Map<String, Object> data) throws Exception {
        if (!isWorking)
        {
            return ;
        }

        final String url = String.format("%s/%s/_doc/%s/_update", this.elasticsearchEndpoint, this.elasticsearchIndex, documentId);

        logger.debug(String.format("update %s", url));

        final HttpResponse response = http.post(url).setDataAsJSON(MyMapUtils.map("doc", data)).execute();

        if (response.statusCode() >= 400) {
            throw new Exception(String.format("elasticsearch sink log error: %d (%s)", response.statusCode(), response.raw()));
        }
    }

    public String getElasticsearchEndpoint() {
        return elasticsearchEndpoint;
    }

    public String getElasticsearchIndex() {
        return elasticsearchIndex;
    }
}
