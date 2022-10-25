package io.datatok.djobi.plugins.logging;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.datatok.djobi.configuration.Configuration;
import io.datatok.djobi.engine.Engine;
import io.datatok.djobi.engine.ExecutionRequest;
import io.datatok.djobi.engine.Workflow;
import io.datatok.djobi.loaders.yaml.YAMLWorkflowLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Provider;

public class ElasticsearchWorkLoggerTest {

    @Inject
    YAMLWorkflowLoader workflowLoader;
    @Inject
    Engine engine;

    @Inject
    Configuration configuration;

    @Inject
    Provider<LoggingPlugin> loggingPluginProvider;

    private Config oldConfig;

    @BeforeEach
    void before() {
        oldConfig = configuration.getConfig();
    }

    @AfterEach
    void after() {
        configuration.setConfig(oldConfig);
    }

    @Test
    void shouldLogSomeWork() throws Exception {
        final Workflow workflow = getWorkflow("mono.yml");

        Assertions.assertEquals(1, workflow.getJobs().size());

        final String configStr = "djobi.plugins.logger { sinks { " +
                "jobs { enabled = true, type = elasticsearch, options { url = \"http://elasticsearch-127-0-0-1.nip.io\", index = \"djobi-jobs\" } } \n" +
                "stages { enabled = true, type = elasticsearch, options { url = \"http://elasticsearch-127-0-0-1.nip.io\", index = \"djobi-stages\" } } \n" +
                "metrics { enabled = false } \n" +
                "" +
                "} }";

        Config newConfig = ConfigFactory
                .parseString(configStr)
                .withFallback(configuration.getConfig());

        configuration.setConfig(newConfig);

        loggingPluginProvider.get().bootstrap();

        engine.run(workflow);

        Assertions.assertEquals(1, workflow.getJobs().size());
    }

    private Workflow getWorkflow(final String workflow) throws Exception {
        return workflowLoader.get(
                ExecutionRequest.build( "./src/test/resources/pipelines/" + workflow)
                        .addArgument("date", "yesterday")
        );
    }
}
