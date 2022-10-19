package io.datatok.djobi.plugins.logging;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;
import io.datatok.djobi.application.ApplicationBuilder;
import io.datatok.djobi.application.Djobi;
import io.datatok.djobi.engine.Engine;
import io.datatok.djobi.engine.Parameter;
import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.engine.PipelineExecutionRequest;
import io.datatok.djobi.engine.parameters.DateParameter;
import io.datatok.djobi.loaders.yaml.YAMLPipelineLoader;
import io.datatok.djobi.plugins.logging.sink.elasticsearch.ElasticsearchLogSink;
import io.datatok.djobi.plugins.logging.subcribers.JobRunFinishSubscriber;
import io.datatok.djobi.plugins.logging.subcribers.JobRunStartSubscriber;
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
        final JobRunStartSubscriber jobRunStartSubscriber = injector.getInstance(JobRunStartSubscriber.class);
        final JobRunFinishSubscriber jobRunFinishSubscriber = injector.getInstance(JobRunFinishSubscriber.class);
        final StageRunSubscriber stageLogger = injector.getInstance(StageRunSubscriber.class);
        final ElasticsearchLogSink esSink = (ElasticsearchLogSink) jobRunStartSubscriber.getSink();
        final ElasticsearchLogSink stageSink = (ElasticsearchLogSink) stageLogger.getSink();
        final ElasticsearchUtils elasticsearchUtils = injector.getInstance(ElasticsearchUtils.class);
        final YAMLPipelineLoader pipelineLoader = injector.getInstance(YAMLPipelineLoader.class);
        final Engine engine = injector.getInstance(Engine.class);

        elasticsearchUtils.deleteIndex(esSink.getSettingUrl(), esSink.getSettingIndex());
        elasticsearchUtils.deleteIndex(stageSink.getSettingUrl(), stageSink.getSettingIndex());

        final Pipeline pipeline =  pipelineLoader.get(
                PipelineExecutionRequest.build("./src/test/resources/pipelines/good_dummy2.yml")
                        .addArgument("date", "yesterday")
        );

        engine.run(pipeline);

        elasticsearchUtils.refresh(esSink.getSettingUrl(), esSink.getSettingIndex());
        elasticsearchUtils.refresh(stageSink.getSettingUrl(), stageSink.getSettingIndex());

        int c = elasticsearchUtils.searchCount(esSink.getSettingUrl(), esSink.getSettingIndex());

        Assertions.assertEquals(1, c);

        assertJobLog(elasticsearchUtils, esSink, pipeline);
    }

    private void assertJobLog(ElasticsearchUtils elasticsearchUtils, ElasticsearchLogSink esSink, Pipeline pipeline) throws Exception {
        Map<String, Object> doc = elasticsearchUtils.query(esSink.getSettingUrl(), esSink.getSettingIndex(), pipeline.getJob(0).getUid());

        doc = (Map<String, Object>) MyMapUtils.browse(doc, "_source");

        Assertions.assertEquals("good_dummy2.yml", MyMapUtils.browse(doc, "pipeline.name"));
    }

}
