package io.datatok.djobi.engine.stages.elasticsearch.output;

import io.datatok.djobi.configuration.Configuration;
import io.datatok.djobi.engine.Engine;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.engine.PipelineExecutionRequest;
import io.datatok.djobi.loaders.yaml.YAMLPipelineLoader;
import io.datatok.djobi.test.MyTestRunner;
import io.datatok.djobi.utils.elasticsearch.ElasticsearchUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.lang.reflect.Method;

@ExtendWith(MyTestRunner.class)
@Tag("IntegrationTest")
public class ElasticsearchEngineTest {

    @Inject
    private Engine engine;

    @Inject
    private YAMLPipelineLoader yamlPipelineLoader;

    @Inject
    private ElasticsearchUtils elasticsearchUtils;

    @Inject
    private Configuration configuration;

    @Test
    void testRunJob() throws Exception {
        elasticsearchUtils.deleteIndex(configuration.getString("elasticsearch"), "out-context_a");

        final Pipeline pipeline = getPipeline("good.yml");

        Method method = Engine.class.getDeclaredMethod("run", Job.class);
        method.setAccessible(true);

        method.invoke(engine, pipeline.getJob(0));

        int c = elasticsearchUtils.searchCount(configuration.getString("elasticsearch"), "out-context_a");

        Assertions.assertEquals(3, c);
/*
        Map doc = (Map) logSink.getDocument(pipeline.getJob(0).getUid()).get("_source");

        Assertions.assertNotNull(doc);
        Assertions.assertEquals("DONE_OK", MyMapUtils.browse(doc, "status"));
        Assertions.assertEquals("DONE_OK", MyMapUtils.browse(doc, "job.post_check_status"));
        Assertions.assertEquals("NO", MyMapUtils.browse(doc, "job.pre_check_status"));
        Assertions.assertEquals("good.yml", MyMapUtils.browse(doc, "pipeline.name"));

 */
    }

    private Pipeline getPipeline(final String pipeline) throws Exception {
        return yamlPipelineLoader.get(
                PipelineExecutionRequest.build( "./src/test/resources/" + pipeline)
                        .addArgument("date", "yesterday")
        );
    }
}
