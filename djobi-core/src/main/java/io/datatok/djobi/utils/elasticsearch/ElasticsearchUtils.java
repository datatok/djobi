package io.datatok.djobi.utils.elasticsearch;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.datatok.djobi.utils.http.Http;
import io.datatok.djobi.utils.http.HttpResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class ElasticsearchUtils {

    @Inject
    private Http http;

    private Map<String, String> serverVersions = new HashMap<>();

    @SuppressWarnings("unchecked")
    public String getElasticsearchServerVersion(final String esHost) throws IOException {
        if (!serverVersions.containsKey(esHost)) {
            serverVersions.put(esHost, (String) http.get(esHost).execute().at("version.number", true));
        }

        return serverVersions.get(esHost);
    }

    public void refresh(final String host, final String index) throws IOException {
        http.post(host + "/" + index + "/_refresh").setTimeout(30 * 1000).execute().close();
    }

    public Map<String, Object> deleteIndex(final String esHost, final String index) throws IOException {
        final String i = index.contains("/") ? (index.split("/")[0]) : index;

        return http.delete(esHost + "/" + i).executeAsDict();
    }

    public int searchCount(final String esHost, final String index) throws IOException {
        return searchCount(esHost, index, null);
    }

    public int searchCount(final String esHost, final String index, final String queryString) throws IOException {
        String url = esHost + "/" +  index + "/_count";

        if (queryString != null && !queryString.isEmpty())
        {
            url = url.concat("?q=").concat(URLEncoder.encode(queryString, "UTF-8"));
        }

        HttpResponse response = http.get(url).execute();

        if (response.statusCode() == 200) {
            final Object count = response.at("count", true);

            return (int) count;
        } else if (response.statusCode() == 404) {
            response.close();
            throw new IOException("index not found");
        } else {
            response.close();
            throw new IOException("Wrong API status " + response.statusCode());
        }
    }

    public Map<String, Object> query(final String esHost, final String index, final String queryString) throws IOException {
        String url = esHost + "/" +  index;

        if (queryString != null && !queryString.isEmpty())
        {
            url = url.concat("/").concat(queryString);
        }

        return http.get(url).executeAsDict();
    }

}
