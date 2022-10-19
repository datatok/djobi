package io.datatok.djobi.engine.actions.net.scp.output;

import com.google.inject.Inject;
import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.check.CheckStatus;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.test.MyTestRunner;
import io.datatok.djobi.test.StageTestUtils;
import io.datatok.djobi.test.mocks.HttpMock;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.io.IOFileUtils;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;

@Tag("UnitTest")
class SCPOutputPostCheckerTest {

    @Inject
    private SCPOutputPostChecker checker;

    @Inject
    private TemplateUtils templateUtils;

    @Inject
    private HttpMock httpMock;

    @BeforeAll
    static void setup() throws Exception {
        final HttpMock httpMock = MyTestRunner.injector.getInstance(HttpMock.class);

        final String responseComponents = IOFileUtils.readInClassPath("files_listing_sample/scp.csv");

        httpMock
            .start(8080)
            .addEndpoint("get_listing", httpMock
                    .endpoint()
                    .ifRequestRegex("/data/files_listing_sample/scp.csv")
                    .withResponseBody(responseComponents)
            )
        ;
    }

    @AfterAll
    static void stopAll() throws IOException {
        final HttpMock httpMock = MyTestRunner.injector.getInstance(HttpMock.class);

        if (httpMock != null) {
            httpMock.shutdown();
        }
    }

    @Test
    void testMissingFile() throws Exception {
        Stage stage = StageTestUtils.getNewStage();

        stage.setParameters(ActionConfiguration.get(SCPOutputConfig.class, new Bag("path", "toto.csv", "check_service", "http_files_listing_local"), stage, templateUtils));

        CheckResult res = checker.postCheck(stage);

        Assertions.assertEquals(CheckStatus.DONE_ERROR, res.getStatus());
        Assertions.assertEquals("not found (toto.csv)!", res.getMeta("reason"));
    }

    @Test
    void testTooSmall() throws Exception {
        Stage stage = StageTestUtils.getNewStage();

        stage.setParameters(ActionConfiguration.get(SCPOutputConfig.class, new Bag("path", "/opt/toto.csv"), stage, templateUtils));

        CheckResult res = checker.postCheck(stage);

        Assertions.assertEquals(CheckStatus.DONE_ERROR, res.getStatus());
        Assertions.assertTrue(res.getMeta("reason").toString().startsWith("empty ("));
    }

    @Test
    void testTooOld() throws Exception {
        Stage stage = StageTestUtils.getNewStage();

        stage.setParameters(ActionConfiguration.get(SCPOutputConfig.class, new Bag("path", "/opt/report_2017_10_10.csv", "check_service", "http_files_listing_local"), stage, templateUtils));

        CheckResult res = checker.postCheck(stage);

        Assertions.assertEquals(CheckStatus.DONE_ERROR, res.getStatus());
        Assertions.assertTrue(res.getMeta("reason").toString().contains("too old"));
    }
}
