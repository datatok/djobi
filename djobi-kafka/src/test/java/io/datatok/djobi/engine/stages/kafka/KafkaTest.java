package io.datatok.djobi.engine.stages.kafka;

import io.datatok.djobi.configuration.Configuration;
import io.datatok.djobi.engine.Engine;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.engine.PipelineExecutionRequest;
import io.datatok.djobi.engine.check.CheckStatus;
import io.datatok.djobi.loaders.yaml.YAMLPipelineLoader;
import io.datatok.djobi.test.MyTestRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.lang.reflect.Method;

@ExtendWith(MyTestRunner.class)
public class KafkaTest {
    @Inject
    private Engine engine;

    @Inject
    private YAMLPipelineLoader yamlPipelineLoader;


    @Inject
    private Configuration configuration;

    @Test
    void shouldRunOutputJobToKafka() throws Exception {
        final Job job = getWorkflow("output.yaml").getJob(0);

        Method method = Engine.class.getDeclaredMethod("run", Job.class);
        method.setAccessible(true);

        method.invoke(engine, job);

        Assertions.assertEquals(job.getPreCheckStatus(), CheckStatus.DONE_OK);
    }

    private Pipeline getWorkflow(final String workflow) throws Exception {
        return yamlPipelineLoader.get(
            PipelineExecutionRequest.build( "./src/test/resources/workflows/" + workflow)
        );
    }
}
