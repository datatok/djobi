package io.datatok.djobi.utils.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.datatok.djobi.application.ApplicationData;
import io.datatok.djobi.configuration.Configuration;
import io.datatok.djobi.utils.JSONUtils;
import io.datatok.djobi.utils.compression.CompressionUtils;
import okhttp3.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpRequest {

    static public final String METHOD_GET = "GET";
    static public final String METHOD_POST = "POST";
    static public final String METHOD_PUT = "PUT";
    static public final String METHOD_DELETE = "DELETE";

    static public final String COMPRESSION_GZIP = "gzip";
    static public final String COMPRESSION_DEFLATE = "deflate";

    static private final Logger logger = Logger.getLogger(HttpRequest.class);

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Inject
    private Configuration configuration;

    /**
     * Dont log as INFO.
     *
     * @since v3.13.0
     */
    private boolean quiet = false;

    private String method;
    private String url;

    /**
     * @since v4.4.0
     *
     * HTTP global timeout.
     */
    private long timeout = 10*1000;

    private Object data;
    private String dataFormat;

    private String dataCompression;

    private String transport = "default";
    private Map<String, String> headers = new HashMap<>();

    private HttpResponse response;

    static Map<String, okhttp3.OkHttpClient> transportPool = new HashMap<>();

    @Inject
    HttpRequest(ApplicationData runData) {
        header("User-Agent", runData.userAgent());
    }

    public HttpRequest init(String method, String url, Object data) {
        this.method = method;
        this.url = url;

        this.setDataAsJSON(data);

        return this;
    }

    public HttpRequest header(String key, String value) {
        if (value == null) {
            this.headers.remove(key);
        } else {
            this.headers.put(key, value);
        }

        return this;
    }

    public HttpRequest setQuiet(boolean quiet) {
        this.quiet = quiet;

        return this;
    }

    public HttpRequest setMethod(String method) {
        this.method = method;

        return this;
    }

    public HttpRequest setUrl(String url) {
        this.url = url;

        return this;
    }

    public HttpRequest setTimeout(long timeout) {
        this.timeout = timeout;

        return this;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public HttpRequest setDataAsJSON(Object data) {
        this.dataFormat = "json";
        this.data = data;

        if (!headers.containsKey("Content-Type") && !headers.containsKey("content-type")) {
            header("Content-Type", "application/json");
        }

        return this;
    }

    public HttpRequest setDataCompression(String dataCompression) {
        this.dataCompression = dataCompression;
        return this;
    }

    public HttpRequest viaTransport(final String transport) {
        this.transport = transport;
        return this;
    }

    public HttpResponse execute() throws IOException {
        final Request.Builder requestBuilder = new Request.Builder();
        RequestBody requestBody = null;

        if (this.data != null) {
            if (this.data instanceof byte[]) {
                requestBody = RequestBody.create((byte[]) this.data);
            } else {
                String dataStr = null;

                try {
                    dataStr = JSONUtils.serialize(data);
                } catch (JsonProcessingException e) {
                    throw new IOException(e.getMessage(), e);
                }

                if (this.dataCompression == null) {
                    requestBody = RequestBody.create(dataStr, JSON);
                } else {
                    byte[] compressedBytes = CompressionUtils.deflate(dataStr);

                    requestBody = RequestBody.create(compressedBytes);
                }
            }
        } else if (this.method.equals(METHOD_POST)) {
            requestBody = RequestBody.create(null, "");
        }

        final URL muRL = new URL(url);

        if (!this.quiet) {
            logger.info(method + " " + url);
        }

        requestBuilder
            .url(muRL)
            .method(method, requestBody)
        ;

        for (Map.Entry<String, String> header : headers.entrySet()) {
            requestBuilder.header(header.getKey(), header.getValue());
        }

        final Request request = requestBuilder.build();
        final Response clientResponse = getTransportClientForURL(muRL).newCall(request).execute();

        response = new HttpResponse(clientResponse);

        return response;
    }

    public HashMap<String, Object> executeAsDict() throws IOException {
        return executeAs(HashMap.class);
    }

    public <Any> Any executeAs(final Class<Any> parseAs) throws IOException {
        return execute().as(parseAs);
    }

    public HttpResponse getResponse() {
        return this.response;
    }

    /**
     *
     * @param url
     * @return
     */
    public OkHttpClient getTransportClientForURL(final URL url) {
        final String poolKey = transport + url.getHost() + timeout;

        if (!transportPool.containsKey(poolKey)) {
            final OkHttpClient.Builder builder = new OkHttpClient.Builder();

            builder
                .addInterceptor(new GzipRequestInterceptor())
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
            ;

            if (this.configuration.hasPath("djobi.http.transports." + this.transport)) {
                final Config configValue = this.configuration.getConfig("djobi.http.transports." + this.transport);

                if (configValue != null) {
                    if (configValue.hasPath("proxy") && configValue.getObject("proxy").size() > 0 && shouldUseWebProxy(url.getHost())) {
                        try {
                            final URL proxyHttpUrl = new URL(configValue.getString("proxy.http"));

                            final InetSocketAddress proxyAddress = new InetSocketAddress(proxyHttpUrl.getHost(), proxyHttpUrl.getPort());
                            builder.proxy (new Proxy(Proxy.Type.HTTP, proxyAddress));

                        } catch(MalformedURLException e) {
                            logger.error("Cannot use proxy", e);
                        }
                    }
                }
            }

            transportPool.put(poolKey, builder.build());
        }

        return transportPool.get(poolKey);
    }

    /**
     * @todo: resolve IP, and check if IP is local or not.
     *
     * @param host
     * @return
     */
    private boolean shouldUseWebProxy(final String host) {
        return !host.equals("127.0.0.1") && host.contains(".");
    }
}
