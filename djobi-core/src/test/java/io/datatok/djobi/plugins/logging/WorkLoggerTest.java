package io.datatok.djobi.plugins.logging;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;
import io.datatok.djobi.application.ApplicationBuilder;
import io.datatok.djobi.application.Djobi;
import io.datatok.djobi.engine.Engine;
import io.datatok.djobi.engine.Parameter;
import io.datatok.djobi.engine.Workflow;
import io.datatok.djobi.engine.ExecutionRequest;
import io.datatok.djobi.engine.parameters.DateParameter;
import io.datatok.djobi.loaders.yaml.YAMLWorkflowLoader;
import io.datatok.djobi.plugins.logging.sink.elasticsearch.ElasticsearchLogSink;
import io.datatok.djobi.plugins.logging.subcribers.JobRunSubscriber;
import io.datatok.djobi.plugins.logging.subcribers.StageRunSubscriber;
import io.datatok.djobi.plugins.report.Reporter;
import io.datatok.djobi.plugins.stages.DefaultActionsPlugin;
import io.datatok.djobi.test.MyTestRunner;
import io.datatok.djobi.test.TestStdoutReporter;
import io.datatok.djobi.test.mocks.HttpMock;
import io.datatok.djobi.utils.JSONUtils;
import io.datatok.djobi.utils.MyMapUtils;
import io.datatok.djobi.utils.elasticsearch.ElasticsearchUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.Map;

@ExtendWith(MyTestRunner.class)
class WorkLoggerTest {

    @Inject
    private HttpMock httpMock;

    @BeforeAll
    static void setup() throws IOException {
        final HttpMock httpMock = MyTestRunner.injector.getInstance(HttpMock.class);

        httpMock
            .start(8080)
            .addEndpoint("home", httpMock
                .endpoint()
                .responseAsJSON()
                .ifRequest("GET", "/")
                .withResponseBody("{ 'name' : 'elasticsearch', 'version' : { 'number' : '7.16.2' } }".replace("'", "\""))
            )
            .addEndpoint("index_delete", httpMock
                .endpoint()
                .responseAsJSON()
                .ifMethod("DELETE")
                .withResponseBody("{ \"acknowledged\":true }")
            )
            .addEndpoint("index_send_document", httpMock
                .endpoint()
                .responseAsJSON()
                .ifMethod("POST")
                .withResponseBody("{ \"acknowledged\":true, \"_id\" : \"toto\" }")
            )
            .addEndpoint("index_send_document", httpMock
                .endpoint()
                .responseAsJSON()
                .ifMethod("PUT")
                .withResponseBody("{ \"acknowledged\":true, \"_id\" : \"toto\" }")
            )
            .addEndpoint("count", httpMock
                .endpoint()
                .responseAsJSON()
                .ifMethod("GET")
                .ifRequestRegex("/([a-z-]+)/_count")
                .withResponseBody("{ \"count\" : 1}")
            )
            .addEndpoint("get_document", httpMock
                .endpoint()
                .responseAsJSON()
                .ifMethod("GET")
                .ifRequestRegex("/djobi-jobs/(.+)")
                .withResponseBody("{ \"_source\" : { \"pipeline\" : { \"name\" : \"good_dummy2.yml\"} } }")
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

    /*@Test void testElasticsearchSink() throws Exception {
        final Pipeline pipeline = getPipeline("good.yml");

        Assertions.assertTrue(logger.isEnabled());

        logger.start(pipeline.getJob(0));

        logger.end(pipeline.getJob(0).getStages().get(0));

        logger.end(pipeline.getJob(0));

        esMockClient.assertDocumentCreated(config.getString("logger.work.io.datatok.djobi.engine.stages.elasticsearch.index"));
    }*/

    @Test
    void testParameterJSONSerialization() throws Exception {
        String json = JSONUtils.serialize(new DateParameter("date", "01/01/2018"));

        Assertions.assertEquals("\"2018-01-01\"", json);

        json = JSONUtils.serialize(MyMapUtils.map("date", new DateParameter("date", "01/01/2018"), "toto", new Parameter("toto", "salut")));

        Assertions.assertEquals("{\"date\":\"2018-01-01\",\"toto\":\"salut\"}", json);
    }

    /**
     * @throws Exception
     * @todo implements log structure tests here.
     */
    @Test
    @Tag("IntegrationTest")
    void testElasticsearchSink() throws Exception {
        final String configStr = "djobi.plugins.logger { sinks { " +
                "jobs { enabled = true, type = elasticsearch, options { url = \"http://localhost:8080\", index = \"djobi-jobs\" } } \n" +
                "stages { enabled = true, type = elasticsearch, options { url = \"http://localhost:8080\", index = \"djobi-stages\" } } \n" +
                "metrics { enabled = false } \n" +
                "" +
                "} }";


        final Djobi application =
            new ApplicationBuilder()
                .configure(
                    ConfigFactory
                        .parseString(configStr)
                        .withFallback(ConfigFactory.load())
                )
                .addPlugin(new DefaultActionsPlugin())
                .addPlugin(new LoggingPlugin())
                .addDependency(Reporter.class, TestStdoutReporter.class)
                .build()
            ;

        final Injector injector = application.getInjector();
        final JobRunSubscriber jobRunStartSubscriber = injector.getInstance(JobRunSubscriber.class);
        final StageRunSubscriber stageLogger = injector.getInstance(StageRunSubscriber.class);
        final ElasticsearchLogSink esSink = (ElasticsearchLogSink) jobRunStartSubscriber.getSink();
        final ElasticsearchLogSink stageSink = (ElasticsearchLogSink) stageLogger.getSink();
        final ElasticsearchUtils elasticsearchUtils = injector.getInstance(ElasticsearchUtils.class);
        final YAMLWorkflowLoader pipelineLoader = injector.getInstance(YAMLWorkflowLoader.class);
        final Engine engine = injector.getInstance(Engine.class);

        elasticsearchUtils.deleteIndex(esSink.getElasticsearchEndpoint(), esSink.getElasticsearchIndex());
        elasticsearchUtils.deleteIndex(stageSink.getElasticsearchEndpoint(), stageSink.getElasticsearchIndex());

        final Workflow workflow =  pipelineLoader.get(
                ExecutionRequest.build("./src/test/resources/pipelines/good_dummy2.yml")
                        .addArgument("date", "yesterday")
        );

        engine.run(workflow);

        elasticsearchUtils.refresh(esSink.getElasticsearchEndpoint(), esSink.getElasticsearchIndex());
        elasticsearchUtils.refresh(stageSink.getElasticsearchEndpoint(), stageSink.getElasticsearchIndex());

        int c = elasticsearchUtils.searchCount(esSink.getElasticsearchEndpoint(), esSink.getElasticsearchIndex());

        Assertions.assertEquals(1, c);

        assertJobLog(elasticsearchUtils, esSink, workflow);
    }

    private void assertJobLog(ElasticsearchUtils elasticsearchUtils, ElasticsearchLogSink esSink, Workflow workflow) throws Exception {
        Map<String, Object> doc = elasticsearchUtils.query(esSink.getElasticsearchEndpoint(), esSink.getElasticsearchIndex(), workflow.getJob(0).getUid());

        doc = (Map<String, Object>) MyMapUtils.browse(doc, "_source");

        Assertions.assertEquals("good_dummy2.yml", MyMapUtils.browse(doc, "pipeline.name"));
    }

}
