package io.datatok.djobi.plugins.logging.sink.elasticsearch;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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

public class ElasticsearchLogSink implements LogSink {

    @Inject
    private Http http;

    static private final Map<String, String> serverVersions = new HashMap<>();
    
    static private final Logger logger = Logger.getLogger(ElasticsearchLogSink.class);

    private String settingUrl;

    private String settingIndex;

    private boolean initialized = false;

    private boolean isWorking = true;

    public ElasticsearchLogSink(final Config config) {
        final Config c = config.withFallback(ConfigFactory.parseMap(MyMapUtils.mapString(
            "url", "http://localhost:9200",
                "index", "djobi-default"
        )));

        this.settingIndex = c.getString("index");
        this.settingUrl = c.getString("url");
    }

    /**
     * Check index name contains type or not, according ES version.
     *
     * @since v3.7.0
     */
    private void init() {
        this.initialized = true;

        if (!this.settingIndex.contains("/")) {
            if (!serverVersions.containsKey(this.settingUrl)) {
                try {
                    serverVersions.put(this.settingUrl, (String) http.get(this.settingUrl).execute().at("version.number"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            final String version = serverVersions.get(this.settingUrl);

            if (version == null || version.startsWith("2")) {
                this.settingIndex += "/doc";
            } else {
                this.settingIndex += "/_doc";
            }
        }
    }

    public Map<String, Object> getDocument(String documentId) throws IOException {
        if (!initialized) {
            init();
        }

        return http.get(String.format("%s/%s/%s",this.settingUrl, this.settingIndex, documentId)).executeAsDict();
    }

    @Override
    public String updateOrCreate(final String documentId, final Map<String, Object> data) throws Exception   {

        if (!isWorking)
        {
            return null;
        }

        if (!initialized) {
            init();
        }

        HttpRequest request;

        if (documentId == null || documentId.isEmpty()) {
            final String url = String.format("%s/%s?refresh",this.settingUrl, this.settingIndex);
            request = http.post(url);
        } else {
            final String url = String.format("%s/%s/%s?refresh",this.settingUrl, this.settingIndex, documentId);
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

        if (!initialized) {
            init();
        }

        return updateOrCreate(null, data);
    }

    @Override
    public void updateAtomic(final String documentId, final Map<String, Object> data) throws Exception {
        if (!isWorking)
        {
            return ;
        }

        if (!initialized) {
            init();
        }

        final String url = String.format("%s/%s/%s/_update", this.settingUrl, this.settingIndex, documentId);

        logger.debug(String.format("update %s", url));

        final HttpResponse response = http.post(url).setDataAsJSON(MyMapUtils.map("doc", data)).execute();

        if (response.statusCode() >= 400) {
            throw new Exception(String.format("elasticsearch sink log error: %d (%s)", response.statusCode(), response.raw()));
        }
    }

    public String getSettingUrl() {
        if (!initialized) {
            init();
        }

        return settingUrl;
    }

    public String getSettingIndex() {
        if (!initialized) {
            init();
        }

        return settingIndex;
    }
}
