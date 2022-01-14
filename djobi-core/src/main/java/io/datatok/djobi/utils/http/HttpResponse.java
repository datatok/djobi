package io.datatok.djobi.utils.http;

import io.datatok.djobi.utils.JSONUtils;
import okhttp3.Headers;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpResponse {
    private Response response;

    private Object parseContentCache;

    private String cachedRawResponse;

    public HttpResponse(Response response) throws IOException {
        this.response = response;

        //raw();

        //response.close();
    }

    public Headers headers() {
        return response.headers();
    }

    public int statusCode() {
        return response.code();
    }

    public InputStream asStream() throws IOException {
        return response.body().byteStream();
    }

    public String raw() throws IOException {
        if (cachedRawResponse == null) {
            cachedRawResponse = response.body() == null ? "" : response.body().string();

            if (response.body() != null) {
                response.close();
            }
        }

        return cachedRawResponse;
    }

    public <Any> Any as(final Class<Any> parseAs) throws IOException {
        if (parseContentCache != null) {
            throw new IOException("content already parsed!");
        }

        parseContentCache = "ok";

        return JSONUtils.parse(raw(), parseAs);
    }

    public Object at(final String field) throws IOException {
        return at(field, false);
    }

    @SuppressWarnings("unchecked")
    public Object at(final String field, final boolean closeBody) throws IOException {
        if (parseContentCache == null) {
            parseContentCache = as(HashMap.class);
        }

        final String[] fields = field.split("\\.");
        Object buffer = parseContentCache;

        for (final String f : fields) {
            if (buffer instanceof Map) {
                buffer = ((Map<String, Object>) buffer).get(f);
            } else if (buffer instanceof List) {
                buffer = ((List) buffer).get(Integer.parseInt(f));
            } else {
                throw new IOException("not a map!");
            }
        }

        if (closeBody) {
            close();
        }

        return buffer;
    }

    public void close() {
        this.response.close();
    }
}
