package io.datatok.djobi.utils.http;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class Http {

    @Inject
    private Provider<HttpRequest> httpRequestProvider;

    public HttpRequest get(final String url) {
        return call(HttpRequest.METHOD_GET, url, null);
    }

    public HttpRequest post(final String url) {
        return post(url, null);
    }

    public HttpRequest post(final String url, final Object data) {
        return call(HttpRequest.METHOD_POST, url, data);
    }

    public HttpRequest put(final String url) {
        return put(url, null);
    }

    public HttpRequest put(final String url, final Object data) {
        return call(HttpRequest.METHOD_PUT, url, data);
    }

    public HttpRequest delete(final String url) {
        return delete(url, null);
    }

    public HttpRequest delete(final String url, final Object data) {
        return call(HttpRequest.METHOD_DELETE, url, data);
    }

    public HttpRequest call(final String method, final String url, final Object data) {
        return httpRequestProvider.get().init(method, url, data);
    }
}
