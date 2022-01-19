package io.datatok.djobi.test.mocks;

import mockwebserver3.MockResponse;
import mockwebserver3.RecordedRequest;
import okio.Buffer;
import org.json.JSONObject;

import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class HttpMockEndpoint {

    Predicate<RecordedRequest> predicate;

    String name;

    final private MockResponse mockResponse = new MockResponse();

    public HttpMockEndpoint withName(String n) {
        name = n;
        return this;
    }

    public HttpMockEndpoint ifMethod(String method) {
        return ifRequest(r -> r.getMethod().toLowerCase().equals(method.toLowerCase()));
    }


    public HttpMockEndpoint ifRequest(String request) {
        return ifRequest(r -> r.getPath().toLowerCase().equals(request.toLowerCase()));
    }

    public HttpMockEndpoint ifRequest(String method, String request) {
        return ifRequest(r -> r.getMethod().toLowerCase().equals(method.toLowerCase())
                && r.getPath().toLowerCase().equals(request.toLowerCase())
        );
    }

    public HttpMockEndpoint ifRequestRegex(String request) {
        return ifRequest(r -> Pattern.matches(request.toLowerCase(), r.getPath().toLowerCase()));
    }

    public HttpMockEndpoint ifRequestRegex(String method, String request) {
        return ifRequest(r -> r.getMethod().toLowerCase().equals(method.toLowerCase())  &&
                Pattern.matches(request.toLowerCase(), r.getPath().toLowerCase())
        );
    }

    public HttpMockEndpoint ifRequest(Predicate<RecordedRequest> predicate) {
        this.predicate = predicate;
        return this;
    }

    public HttpMockEndpoint withResponseBody(String responseBody) {
        mockResponse.setBody(responseBody);
        return this;
    }

    public HttpMockEndpoint withResponseBodyAsJSON(Map responseBody) {
        mockResponse.setBody(new JSONObject(responseBody).toString());
        return responseAsJSON();
    }

    public HttpMockEndpoint withResponseBodyFromRequest() {
        return withResponseBody("__request__");
    }

    public HttpMockEndpoint responseAsJSON() {
        return withResponseHeader("content-type", "application/json");
    }

    public HttpMockEndpoint withResponseHeader(final String n, final String v) {
        mockResponse.setHeader(n, v);
        return this;
    }

    public HttpMockEndpoint withResponseCode(int code) {
        mockResponse.setResponseCode(code);
        return this;
    }

    public HttpMockEndpoint withResponseBuffer(Buffer buf) {
        mockResponse.setBody(buf);
        return this;
    }

    public MockResponse getMockResponse() {
        return mockResponse;
    }
}
