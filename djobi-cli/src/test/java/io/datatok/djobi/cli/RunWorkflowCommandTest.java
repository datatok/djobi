package io.datatok.djobi.cli;

import com.google.inject.Inject;
import io.datatok.djobi.cli.utils.WorkflowRequestFactory;
import io.datatok.djobi.engine.ExecutionRequest;
import io.datatok.djobi.plugins.report.OutVerbosity;
import io.datatok.djobi.plugins.report.VerbosityLevel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static com.github.stefanbirkner.systemlambda.SystemLambda.*;

@ExtendWith(CLITestRunner.class)
public class RunWorkflowCommandTest {

    @Inject
    private CommandKernel commandKernel;

    @Inject
    private OutVerbosity outVerbosity;

    @Inject
    WorkflowRequestFactory workflowRequestFactory;

    private final ByteArrayOutputStream myOut = new ByteArrayOutputStream();

    @BeforeEach
    void setup() {
        System.setOut(new PrintStream(myOut));
    }

    @Test
    void runNormal() {
        commandKernel.run("run", "./src/test/resources/pipelines/mono.yml");

        Assertions.assertEquals(outVerbosity.getVerbosityLevel(), VerbosityLevel.NORMAL);
        Assertions.assertTrue(outVerbosity.isNotQuiet());
        Assertions.assertFalse(outVerbosity.isVerbose());
    }

    @Test
    void runWithMetaAndArgument() {
        commandKernel.run("run", "-a", "date=yesterday", "./src/test/resources/pipelines/mono.yml");

        ExecutionRequest lastObj = workflowRequestFactory.getLastObjectBuilt();

        Assertions.assertEquals(outVerbosity.getVerbosityLevel(), VerbosityLevel.NORMAL);
        Assertions.assertTrue(outVerbosity.isNotQuiet());
        Assertions.assertFalse(outVerbosity.isVerbose());

        Assertions.assertEquals("yesterday", lastObj.getArgument("date"));
    }

    @Test
    void runPipelinePathFromEnv() throws Exception {
        withEnvironmentVariable("DJOBI_PIPELINE", "./src/test/resources/pipelines/mono.yml")
            .execute(() -> {
                commandKernel.run("run");

                ExecutionRequest lastObj = workflowRequestFactory.getLastObjectBuilt();

                Assertions.assertEquals("./src/test/resources/pipelines/mono.yml", lastObj.getDefinitionURI());
            });
    }

    @Test
    void runVerbose() {
        commandKernel.run("run", "-v", "./src/test/resources/pipelines/mono.yml");

        Assertions.assertEquals(outVerbosity.getVerbosityLevel(), VerbosityLevel.VERBOSE);
        Assertions.assertTrue(outVerbosity.isNotQuiet());
        Assertions.assertTrue(outVerbosity.isVerbose());
        Assertions.assertFalse(outVerbosity.isVeryVerbose());
    }

    @Test
    void runVeryVerbose() {
        commandKernel.run("run", "-vv", "./src/test/resources/pipelines/mono.yml");

        Assertions.assertEquals(outVerbosity.getVerbosityLevel(), VerbosityLevel.VERY_VERBOSE);
        Assertions.assertTrue(outVerbosity.isVerbose());
        Assertions.assertTrue(outVerbosity.isVeryVerbose());
    }

}
