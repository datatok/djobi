package io.datatok.djobi.cli.utils;

import com.google.inject.Inject;
import io.datatok.djobi.cli.CLITestRunner;
import io.datatok.djobi.engine.ExecutionRequest;
import io.datatok.djobi.utils.EnvProvider;
import io.datatok.djobi.utils.MyMapUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;

@ExtendWith(CLITestRunner.class)
public class WorkflowRequestFactoryTest {

    @Inject
    WorkflowRequestFactory workflowRequestFactory;

    @Inject
    EnvProvider envProvider;

    @Test
    public void testCLIArgsAndEnv() {
        envProvider
            .clearScopedCache()
            .setScoped("META_TITLE", "hello world")
            .setScoped("ARG_DATE", "today")
            .setScoped("ARG_TITLE", "from env")
        ;

        ExecutionRequest pipelineExecutionRequest = workflowRequestFactory.build(
            "",
            MyMapUtils.mapString("date", "yesterday"),
            MyMapUtils.mapString(),
    "a,b",
    "",
            ""
        );

        Assertions.assertEquals("yesterday", pipelineExecutionRequest.getArgument("date"));
        Assertions.assertEquals("from env", pipelineExecutionRequest.getArgument("title"));
        Assertions.assertEquals(Arrays.asList("a", "b"), pipelineExecutionRequest.getJobsFilter());
        Assertions.assertEquals("hello world", pipelineExecutionRequest.getMetaDataLabel("title"));
    }

}
