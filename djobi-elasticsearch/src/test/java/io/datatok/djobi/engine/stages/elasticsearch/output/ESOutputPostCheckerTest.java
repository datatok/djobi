package io.datatok.djobi.engine.stages.elasticsearch.output;

import io.datatok.djobi.configuration.Configuration;
import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.check.CheckStatus;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.test.MyTestRunner;
import io.datatok.djobi.test.StageTestUtils;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.MyMapUtils;
import io.datatok.djobi.utils.http.Http;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.io.IOException;

@ExtendWith(MyTestRunner.class)
@Tag("IntegrationTest")
public class ESOutputPostCheckerTest {

    private static Logger logger = Logger.getLogger(ESOutputPostCheckerTest.class);

    @Inject
    private Configuration configuration;

    @Inject
    private ESOutputPostChecker checker;

    @Inject
    private TemplateUtils templateUtils;

    @Inject
    private Http http;

    private String elasticsearchUrl = "elasticsearch:9200";

    @BeforeEach
    void before() {
        this.elasticsearchUrl = configuration.getString("elasticsearch");

        try {
            http.delete(elasticsearchUrl + "/items").execute().close();
            http.delete(elasticsearchUrl + "/toto").execute().close();
        } catch(IOException e) {
            logger.error(e.getMessage());
        }
    }

    @AfterEach
    void after() {
        try {
            http.delete(elasticsearchUrl + "/items").execute().close();
        } catch(IOException e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    void testWrong() throws Exception {
        Stage stage = StageTestUtils.getNewStage();

        stage.setParameters(ActionConfiguration.get(ESOutputConfig.class, new Bag("host", "blabla:9200", "index", "toto"), stage, templateUtils));

        CheckResult res = null;
        try {
            res = checker.postCheck(stage);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        Assertions.assertNotNull(res);
        Assertions.assertEquals(CheckStatus.DONE_ERROR, res.getStatus());
    }


    @Test
    void testIndexNotFound() throws Exception {
        Stage stage = StageTestUtils.getNewStage();

        stage.setParameters(ActionConfiguration.get(ESOutputConfig.class, new Bag("host", elasticsearchUrl, "index", "toto"), stage, templateUtils));

        CheckResult res = checker.postCheck(stage);

        Assertions.assertEquals(CheckStatus.DONE_ERROR, res.getStatus());
        Assertions.assertTrue(res.getMeta("reason").toString().toLowerCase().contains("index not found"));
    }

    @Test
    void test() throws Exception {
        final String es = elasticsearchUrl;

        http.delete(es + "/items").execute().close();
        http.put(es + "/items/items/1?refresh").setDataAsJSON(MyMapUtils.map("hello", "world")).execute().close();
        http.put(es + "/items/items/2?refresh").setDataAsJSON(MyMapUtils.map("hello", "monde")).execute().close();
        http.post(es + "/items/items/_flush", "{}").execute().close();

        Stage stage = StageTestUtils.getNewStage();

        stage.setParameters(ActionConfiguration.get(ESOutputConfig.class, new Bag("host", elasticsearchUrl, "index", "items", "clean_query", "hello:world"), stage, templateUtils));

        CheckResult res = checker.postCheck(stage);

        Assertions.assertEquals(CheckStatus.DONE_OK, res.getStatus());
        Assertions.assertEquals(1, res.getMeta("value"));
    }
}
