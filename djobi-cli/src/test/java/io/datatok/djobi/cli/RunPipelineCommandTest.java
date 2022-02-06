package io.datatok.djobi.cli;

import com.google.inject.Inject;
import io.datatok.djobi.plugins.report.OutVerbosity;
import io.datatok.djobi.plugins.report.VerbosityLevel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class RunPipelineCommandTest {

    @Inject
    private CommandFactory commandFactory;

    @Inject
    private OutVerbosity outVerbosity;

    private final ByteArrayOutputStream myOut = new ByteArrayOutputStream();

    @BeforeEach
    void setup() {
        System.setOut(new PrintStream(myOut));
    }

    @Test
    void runNormal() {
        commandFactory.run(new String[]{"run", "./src/test/resources/pipelines/mono.yml"});

        Assertions.assertEquals(outVerbosity.getVerbosityLevel(), VerbosityLevel.NORMAL);
        Assertions.assertTrue(outVerbosity.isNotQuiet());
        Assertions.assertFalse(outVerbosity.isVerbose());
    }

    @Test
    void runVerbose() {
        commandFactory.run(new String[]{"run", "-v", "./src/test/resources/pipelines/mono.yml"});

        Assertions.assertEquals(outVerbosity.getVerbosityLevel(), VerbosityLevel.VERBOSE);
        Assertions.assertTrue(outVerbosity.isNotQuiet());
        Assertions.assertTrue(outVerbosity.isVerbose());
        Assertions.assertFalse(outVerbosity.isVeryVerbose());
    }

    @Test
    void runVeryVerbose() {
        commandFactory.run(new String[]{"run", "-vv", "./src/test/resources/pipelines/mono.yml"});

        Assertions.assertEquals(outVerbosity.getVerbosityLevel(), VerbosityLevel.VERY_VERBOSE);
        Assertions.assertTrue(outVerbosity.isVerbose());
        Assertions.assertTrue(outVerbosity.isVeryVerbose());
    }

}