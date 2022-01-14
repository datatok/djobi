package io.datatok.djobi.engine.stages;

import com.google.inject.Inject;
import io.datatok.djobi.plugins.stages.api.ambari.APIAmbariType;
import io.datatok.djobi.test.ActionTest;
import io.datatok.djobi.test.MyTestRunner;
import io.datatok.djobi.test.StageTestUtils;
import io.datatok.djobi.test.mocks.HttpMock;
import io.datatok.djobi.utils.Bag;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class AmbariAPITest extends ActionTest {

    @Inject
    private StageTestUtils stageTestUtils;

    @Inject
    private HttpMock httpMock;

    @BeforeAll
    static private void setup() throws IOException {
        final HttpMock httpMock = MyTestRunner.injector.getInstance(HttpMock.class);

        final String responseComponents =  "{\n" +
                "  \"href\": \"http://localhost:8080/api/v1/clusters/DataHub/services/SPARK/components/SPARK_THRIFTSERVER\",\n" +
                "  \"ServiceComponentInfo\": {\n" +
                "    \"category\": \"SLAVE\",\n" +
                "    \"cluster_name\": \"DataHub\",\n" +
                "    \"component_name\": \"SPARK_THRIFTSERVER\",\n" +
                "    \"display_name\": \"Spark Thrift Server\",\n" +
                "    \"init_count\": 0,\n" +
                "    \"install_failed_count\": 0,\n" +
                "    \"installed_count\": 0,\n" +
                "    \"recovery_enabled\": \"false\",\n" +
                "    \"service_name\": \"SPARK\",\n" +
                "    \"started_count\": 1,\n" +
                "    \"state\": \"STARTED\",\n" +
                "    \"total_count\": 1,\n" +
                "    \"unknown_count\": 0\n" +
                "  },\n" +
                "  \"host_components\": [\n" +
                "    {\n" +
                "      \"href\": \"http://localhost:8080/api/v1/clusters/DataHub/hosts/my-host/host_components/SPARK_THRIFTSERVER\",\n" +
                "      \"HostRoles\": {\n" +
                "        \"cluster_name\": \"DataHub\",\n" +
                "        \"component_name\": \"SPARK_THRIFTSERVER\",\n" +
                "        \"host_name\": \"my-host\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        final String responseRestartAction = "{\n" +
                "  \"href\": \"http://localhost:8080/api/v1/clusters/datahub_dev/requests/102\",\n" +
                "  \"Requests\": {\n" +
                "    \"id\": 102,\n" +
                "    \"status\": \"Accepted\"\n" +
                "  }\n" +
                "}";

        httpMock
                .start(8080)
                .addEndpoint("get_components", httpMock
                        .endpoint()
                        .responseAsJSON()
                        .ifRequestRegex("/api/v1/clusters/([a-z]*)/services/([a-z]*)/components/([a-z]*)\\??")
                        .withResponseBody(responseComponents)
                )
                .addEndpoint("action_restart", httpMock
                        .endpoint()
                        .responseAsJSON()
                        .ifRequestRegex("/api/v1/clusters/([a-z]*)/requests\\??")
                        .withResponseBody(responseRestartAction)
                )
        ;
    }

    @AfterAll
    static private void stopAll() throws IOException {
        final HttpMock httpMock = MyTestRunner.injector.getInstance(HttpMock.class);

        if (httpMock != null) {
            httpMock.shutdown();
        }
    }

    @Test void test() throws Exception {
        stageTestUtils.run(APIAmbariType.TYPE, new Bag(
                "action", "restart",
                        "url", "http://localhost:8080",
                        "cluster", "datahub",
                        "service", "spark",
                        "component", "thrift"));

        Assertions.assertTrue(httpMock.getRequestsByEndpoint("action_restart").size() > 0);
    }

}
