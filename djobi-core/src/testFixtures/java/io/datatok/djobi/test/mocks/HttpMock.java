package io.datatok.djobi.test.mocks;

import com.google.inject.Singleton;
import mockwebserver3.Dispatcher;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class HttpMock extends Dispatcher {

    static Logger logger = Logger.getLogger(HttpMock.class);

    private MockWebServer httpMockClient;

    private List<HttpMockEndpoint> endpoints;

    private Map<String, List<RecordedRequest>> requestByEndpoint;

    public HttpMock start() throws IOException {
        return start(8080);
    }

    public HttpMock start(int port) throws IOException {
        if (httpMockClient != null) {
            httpMockClient.shutdown();
            httpMockClient = null;
        }

        httpMockClient = new MockWebServer();
        httpMockClient.start(port);

        httpMockClient.setDispatcher(this);

        endpoints = new ArrayList<>();
        requestByEndpoint = new HashMap<>();

        return this;
    }

    public HttpMockEndpoint endpoint() {
        return new HttpMockEndpoint();
    }

    public HttpMock addEndpoint(String name, HttpMockEndpoint endpoint) {
        endpoints.add(endpoint.withName(name));

        return this;
    }

    @Override
    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        for (HttpMockEndpoint endpoint : endpoints) {

            if (endpoint.predicate == null || endpoint.predicate.test(request)) {
                final String k = endpoint.name;

                if (!requestByEndpoint.containsKey(k)) {
                    requestByEndpoint.put(k, new ArrayList<>());
                }

                requestByEndpoint.get(k).add(request);

                logger.info(String.format("match %s request", k));

                return endpoint.getMockResponse();
            }
        }

        return new MockResponse().setResponseCode(404);
    }

    public List<RecordedRequest> getRequestsByEndpoint(final String endpoint) {
        return requestByEndpoint.get(endpoint);
    }

    public HttpMock stop() throws IOException {
        httpMockClient.shutdown();

        return this;
    }

    public MockWebServer getClient() {
        return httpMockClient;
    }

}
