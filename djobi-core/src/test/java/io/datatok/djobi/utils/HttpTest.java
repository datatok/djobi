package io.datatok.djobi.utils;

import com.google.inject.Inject;
import io.datatok.djobi.test.HttpNumbersResponse;
import io.datatok.djobi.test.MyTestRunner;
import io.datatok.djobi.test.mocks.HttpMock;
import io.datatok.djobi.utils.http.Http;
import io.datatok.djobi.utils.http.HttpRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.Map;

@ExtendWith(MyTestRunner.class)
class HttpTest {

    @Inject
    private Http http;

    @Inject
    private HttpMock httpMock;

    @BeforeAll
    static void setup() throws IOException {
        final HttpMock httpMock = MyTestRunner.injector.getInstance(HttpMock.class);

        httpMock
            .start(8080)
            .addEndpoint("number", httpMock.endpoint().responseAsJSON().ifRequest("/numbers").withResponseBody("{\"_int\": 1, \"_float\" : 1.2, \"_string\" : \"1\", \"_extra\" : \"nothing here!\"}"))
            .addEndpoint("1", httpMock.endpoint().responseAsJSON().ifRequest("/1").withResponseBody("{\"name\":\"mock\"}"))
            .addEndpoint("post", httpMock.endpoint().ifRequest("/_test_").responseAsJSON().withResponseBodyFromRequest())
        ;
    }

    @AfterAll
    static void stopAll() throws IOException {
        final HttpMock httpMock = MyTestRunner.injector.getInstance(HttpMock.class);

        if (httpMock != null) {
            httpMock.shutdown();
        }
    }

    @Test void testSimpleGetHttp() throws Exception {
        HttpRequest request = http.get("http://localhost:8080/1");
        Map out = request.execute().as(Map.class);

        Assertions.assertNotNull(out);
        Assertions.assertEquals("mock", out.get("name"));
    }

    @Test void testSimpleNumbers() throws Exception {
        HttpRequest request = http.get("http://localhost:8080/numbers");
        HttpNumbersResponse out = request.execute().as(HttpNumbersResponse.class);

        Assertions.assertNotNull(out);
        Assertions.assertEquals(1, out._int.intValue());
        Assertions.assertEquals(Float.valueOf((float) 1.2), out._float);
        Assertions.assertEquals("1", out._string);
    }

    @Test void testSimpleGetWeb() throws Exception {
        http.get("https://api.github.com").viaTransport("web").execute();
    }

    @Test void testPost() throws Exception {
        final Object data = MyMapUtils.map("hello", "world");

        http.post("http://localhost:8080/_test_", data).execute();

        Assertions.assertTrue(httpMock.getRequestsByEndpoint("post").size() == 1);
    }
}
