package io.datatok.djobi.loaders.yaml;

import com.google.inject.Inject;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.engine.PipelineExecutionRequest;
import io.datatok.djobi.engine.check.CheckStatus;
import io.datatok.djobi.executors.LocalExecutor;
import io.datatok.djobi.spark.executor.SparkExecutor;
import io.datatok.djobi.test.executor.DummyExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.util.Arrays;

class YAMLPipelineLoaderTest {

    @Inject
    private YAMLPipelineLoader yamlPipelineLoader;

    @Test void testGetDefinitionWrongFile() {
        Assertions.assertThrows(IOException.class, () -> load("toto"));
        Assertions.assertThrows(YAMLException.class, () -> load("wrong1"));
        Assertions.assertThrows(IOException.class, () -> load("wrong2.yml"));
    }

    @Test void executor() throws Exception {
        Assertions.assertEquals(LocalExecutor.class, load("test_outputs.yml").getExecutor().getClass());
        Assertions.assertEquals(DummyExecutor.class, load("good_dummy.yml").getExecutor().getClass());
        Assertions.assertEquals(SparkExecutor.class, load("good_1.yml").getExecutor().getClass());
    }

    @Test void testWithoutContext() throws Exception {
        final Pipeline p = load("good_1.yml");

        Assertions.assertNotNull(p);

        Assertions.assertEquals(1, p.getJobs().size());
        Assertions.assertEquals(6, p.getJobs().get(0).getParameters().size());
        Assertions.assertEquals("_default_", p.getJobs().get(0).getParameters().get("_context_").getValue());
    }

    @Test()
    void testWithContexts() throws Exception {
        final Pipeline p = load("good.yml");

        Assertions.assertNotNull(p);

        // Test parameters
        Assertions.assertEquals("context_a", p.getJobs().get(0).getParameters().get("pd").getValue());
        Assertions.assertEquals("context_b", p.getJobs().get(1).getParameters().get("pd").getValue());
        Assertions.assertEquals("p1", p.getJobs().get(0).getParameters().get("p1").getValue());
        Assertions.assertEquals("a", p.getJobs().get(0).getParameters().get("pjc1").getValue());
        Assertions.assertNotNull(p.getJobs().get(0).getParameters().get("year"));
        Assertions.assertNotNull(p.getJobs().get(0).getParameters().get("month"));
        Assertions.assertNotNull(p.getJobs().get(0).getParameters().get("day"));

        // Test stages
        final Job job1  = p.getJobs().get(0);

        Assertions.assertNotNull(job1.getId());
        Assertions.assertEquals("job1_a", job1.getId());
        Assertions.assertEquals(5, job1.getStages().size());
        Assertions.assertEquals("org.spark.mutate", job1.getStages().get(1).getKind());
        Assertions.assertEquals("fs-output", job1.getStages().get(4).getKind());
        Assertions.assertNotNull(job1.getStages().get(0).getJob());

        Assertions.assertEquals(CheckStatus.TODO, job1.getStages().get(1).getPreCheck().getStatus());
    }

    @Test()
    void testWithFilter() throws Exception {
        Assertions.assertEquals(2, load("good.yml", "job1_(a|b)").getJobs().size());
        Assertions.assertEquals(2, load("good.yml", "job1_a,job1_b").getJobs().size());
        Assertions.assertEquals(2, load("good.yml", "job1_(.*)").getJobs().size());
        Assertions.assertEquals(1, load("good.yml", "job1_a").getJobs().size());
        Assertions.assertEquals(0, load("good.yml", "not_existing").getJobs().size());
    }

    @Test()
    void testLabels() throws Exception {
        final Pipeline p = load("good.yml");

        Assertions.assertNotNull(p);

        final Job job1  = p.getJobs().get(0);

        Assertions.assertEquals("t.decaux", p.getLabel("io.datatok/org-author"));

        Assertions.assertEquals("low", job1.getLabel("level"));

        Assertions.assertEquals("input", job1.getStages().get(0).getLabel("io.datatok/stage-type"));
    }

    private Pipeline load(final String pipeline) throws Exception {
        return load(pipeline, "");
    }

    private Pipeline load(final String pipeline, final String jobFilter) throws Exception {
        return yamlPipelineLoader.get(
                PipelineExecutionRequest.build( "./src/test/resources/pipelines/" + pipeline)
                        .setJobsFilter(Arrays.asList(jobFilter.split(",")))
                        .addArgument("date", "yesterday")
        );
    }
}
