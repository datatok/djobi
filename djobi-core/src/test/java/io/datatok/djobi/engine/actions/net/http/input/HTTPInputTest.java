package io.datatok.djobi.engine.actions.net.http.input;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.engine.PipelineExecutionRequest;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.test.ActionTest;
import io.datatok.djobi.test.MyTestRunner;
import io.datatok.djobi.test.mocks.HttpMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;

public class HTTPInputTest extends ActionTest {

    @Inject
    Provider<HTTPInputRunner> runnerProvider;

    @Inject
    private HttpMock httpMock;

    @BeforeAll
    static private void setup() throws Exception {
        final HttpMock httpMock = MyTestRunner.injector.getInstance(HttpMock.class);

        final String responseComponents = "[{\"letter\" : \"A\", \"count\" : 12},{\"letter\" : \"B\", \"count\" : 1}, {\"letter\" : \"C\", \"count\" : 2}]";

        httpMock
                .start(8080)
                .addEndpoint("get_json", httpMock
                        .endpoint()
                        .ifRequestRegex("/get_json")
                        .withResponseBody(responseComponents)
                )
                .addEndpoint("login", httpMock
                        .endpoint()
                        .ifRequestRegex("/login")
                        .withResponseBody("{}")
                        .withResponseHeader("Set-Cookie", "token=1234567890; Max-Age=60; Path=/; Version=1 ")
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

    @Test
    public void testQueryBuilder() throws Exception {
        final Pipeline pipeline = yamlPipelineLoader.get(
                PipelineExecutionRequest.build("./src/test/resources/pipelines/http_input.yml")
                        .addArgument("date", "05/02/2017")
        );

        final Job job = pipeline.getJob(0);
        final Stage httpInputStage = job.getStages().get(0);
        final HTTPInputConfig config = new HTTPInputConfig(httpInputStage.getSpec(), job, templateUtils);

        Assertions.assertEquals("/get_json", config.URLPath);
        Assertions.assertEquals("endDate=2017-02-05&region=FR&startDate=2017-02-05&timezone=UTC", config.URLQueryString);

        httpInputStage.setParameters(config);

        Assertions.assertEquals("http.input", httpInputStage.getKind());

        ActionRunResult runResult = runnerProvider.get().run(httpInputStage, null, getSparkExecutor());
        StageData<?> stageData = runResult.getData();

        Assertions.assertEquals("[{\"letter\" : \"A\", \"count\" : 12},{\"letter\" : \"B\", \"count\" : 1}, {\"letter\" : \"C\", \"count\" : 2}]", stageData.getData());
    }

}
