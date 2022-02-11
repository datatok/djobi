package io.datatok.djobi.cli.utils;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.datatok.djobi.engine.PipelineExecutionRequest;
import io.datatok.djobi.utils.EnvProvider;
import io.datatok.djobi.utils.MyMapUtils;
import org.apache.commons.collections.ListUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class PipelineRequestFactoryTest {

    @Inject
    PipelineRequestFactory pipelineRequestFactory;

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

        PipelineExecutionRequest pipelineExecutionRequest = pipelineRequestFactory.build(
            "",
            MyMapUtils.mapString("date", "yesterday"),
            MyMapUtils.mapString(),
    "a,b",
    "",
            new boolean[]{}
        );

        Assertions.assertEquals("yesterday", pipelineExecutionRequest.getRaw().get("date"));
        Assertions.assertEquals("from env", pipelineExecutionRequest.getRaw().get("title"));
        Assertions.assertEquals(Arrays.asList("a", "b"), pipelineExecutionRequest.getJobsFilter());
        Assertions.assertEquals("hello world", pipelineExecutionRequest.getMeta().get("title"));
    }

}
