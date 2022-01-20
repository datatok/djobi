package io.datatok.djobi.plugins.logging;

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
import io.datatok.djobi.utils.JSONUtils;
import io.datatok.djobi.utils.MyMapUtils;
import io.datatok.djobi.utils.elasticsearch.ElasticsearchUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

@ExtendWith(MyTestRunner.class)
class WorkLoggerTest {

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

        final String configPrefix = "djobi.plugins.logger.sinks.";

        final Djobi application =
                new ApplicationBuilder()
                        .configure(ConfigFactory.parseMap(
                                MyMapUtils.mapString(
                                        configPrefix + LoggerTypes.TYPE_JOBS + ".type", "elasticsearch",
                                        configPrefix + LoggerTypes.TYPE_STAGES + ".type", "elasticsearch",
                                        configPrefix + LoggerTypes.TYPE_METRICS + ".type", ""
                                )
                        ).withFallback(ConfigFactory.load().resolveWith(ConfigFactory.parseMap(MyMapUtils.mapString("loggerType", "elasticsearch")))))
                        .addPlugin(new DefaultActionsPlugin())
                        .addPlugin(new LoggingPlugin())
                        .addDependency(Reporter.class, TestStdoutReporter.class)
                        .build();

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

        assertStageLog(elasticsearchUtils, stageSink, pipeline);
    }

    private void assertJobLog(ElasticsearchUtils elasticsearchUtils, ElasticsearchLogSink esSink, Pipeline pipeline) throws Exception {
        Map<String, Object> doc = elasticsearchUtils.query(esSink.getSettingUrl(), esSink.getSettingIndex(), pipeline.getJob(0).getUid());

        doc = (Map<String, Object>) MyMapUtils.browse(doc, "_source");

        Assertions.assertEquals("good_dummy2.yml", MyMapUtils.browse(doc, "pipeline.name"));
    }

    private void assertStageLog(ElasticsearchUtils elasticsearchUtils, ElasticsearchLogSink esSink, Pipeline pipeline) throws Exception {
        Map<String, Object> result = elasticsearchUtils.query(esSink.getSettingUrl(), esSink.getSettingIndex(), "_search?q=job.uid:" + pipeline.getJob(0).getUid());

        Assertions.assertNotNull(result);

        List hits = (List) MyMapUtils.browse(result, "hits.hits");

        Assertions.assertEquals(3, hits.size());
    }

}
