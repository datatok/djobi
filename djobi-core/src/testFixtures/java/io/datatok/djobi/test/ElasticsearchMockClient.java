package io.datatok.djobi.test;

import com.google.inject.Singleton;
import io.datatok.djobi.test.mocks.HttpMock;
import mockwebserver3.MockResponse;
import mockwebserver3.RecordedRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class ElasticsearchMockClient {

    private final HttpMock client;

    static public String version = "2.4.6";
    static public String clusterName = "mock";

    public ElasticsearchMockClient() {
        this.client = new HttpMock();
    }

    public void start() throws IOException {
        this.client.start(9200);

        client
            .addEndpoint("count_0", client.endpoint().ifRequest(r -> r.getPath().contains("/_count")).withResponseBodyAsJSON(
                map("count", 0)))
            .addEndpoint("search_0", client.endpoint().ifRequest(r -> r.getPath().contains("/_search")).withResponseBodyAsJSON(
                map("took", 10, "hits" , map("total", 0))))
            .addEndpoint("refresh", client.endpoint().ifRequest(r -> r.getPath().contains("/_refresh")).withResponseBodyAsJSON(
                map("_shards", map("total", 100, "successful", 100, "failed", 0))
            ))
            .addEndpoint("bulk", client.endpoint().ifRequest(r -> r.getPath().contains("/_bulk")).withResponseBodyAsJSON(
                map("took", 10, "errors", false, "items", new JSONArray())
            ))
            .addEndpoint("put_document", client.endpoint().ifRequest("put", "(.*)\\/(.*)\\/(.*)").withResponseBodyAsJSON(
                map("_id", "new_doc_id", "_type", "type", "_index", "index")
            ))
            .addEndpoint("put_index", client.endpoint().ifMethod("put").withResponseBodyAsJSON(
                map("acknowledged", true, "shards_acknowledged", true)
            ))
            .addEndpoint("default", client.endpoint().withResponseBodyAsJSON(
                map("name", "mock", "cluster_name", clusterName, "version", map("number", version))
            ))
        ;
    }

    public void stop() {
        try {
            client.stop();
        } catch (IOException e) {

        }
    }


    public RecordedRequest getIndexCreatedRequest(final String index) throws AssertionError {
        for (RecordedRequest request : this.client.getRequestsByEndpoint("put_index")) {
            if (request.getPath().startsWith("/" + index + "/")) {
                // withHeader("Content-Length", Integer.toString(contentLength))
                return request;
            }
        }

        return null;
    }

    public RecordedRequest getBulkRequest(final String index, final int contentLength) throws AssertionError {
        for (RecordedRequest request : this.client.getRequestsByEndpoint("bulk")) {
            if (request.getPath().startsWith("/" + index + "/")) {
                // withHeader("Content-Length", Integer.toString(contentLength))
                return request;
            }
        }

        return null;
    }

    private MockResponse toHttpResponse(final JSONObject data) {
        return new MockResponse()
                .setBody(data.toString())
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                //.withConnectionOptions(ConnectionOptions.connectionOptions().withSuppressConnectionHeader(true).withKeepAliveOverride(false))
                .setHeader("Keep-Alive", "timeout=1, max=1")
        ;
    }

    private <K, V> Map<K, V> map(Object... args) {
        Map<K, V> res = new HashMap<>();
        K key = null;
        for (Object arg : args) {
            if (key == null) {
                key = (K) arg;
            } else {
                res.put(key, (V) arg);
                key = null;
            }
        }
        return res;
    }
}
