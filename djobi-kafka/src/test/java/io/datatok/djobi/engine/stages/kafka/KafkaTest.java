package io.datatok.djobi.engine.stages.kafka;

import io.datatok.djobi.configuration.Configuration;
import io.datatok.djobi.engine.Engine;
import io.datatok.djobi.engine.ExecutionRequest;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Workflow;
import io.datatok.djobi.engine.check.CheckStatus;
import io.datatok.djobi.loaders.yaml.YAMLWorkflowLoader;
import io.datatok.djobi.test.MyTestRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.lang.reflect.Method;

@ExtendWith(MyTestRunner.class)
@Tag("IntegrationTest")
public class KafkaTest {
    @Inject
    private Engine engine;

    @Inject
    private YAMLWorkflowLoader yamlPipelineLoader;


    @Inject
    private Configuration configuration;

    @Test
    void shouldRunOutputJobToKafka() throws Exception {
        final Job job = getWorkflow("output.yaml").getJob(0);

        Method method = Engine.class.getDeclaredMethod("run", Job.class);
        method.setAccessible(true);

        method.invoke(engine, job);

        Assertions.assertEquals(CheckStatus.DONE_OK, job.getPreCheckStatus());
    }

    private Workflow getWorkflow(final String workflow) throws Exception {
        return yamlPipelineLoader.get(
            ExecutionRequest.build( "./src/test/resources/workflows/" + workflow)
        );
    }
}
