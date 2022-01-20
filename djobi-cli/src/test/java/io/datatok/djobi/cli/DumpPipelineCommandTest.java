package io.datatok.djobi.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.datatok.djobi.cli.commands.DumpPipelineCommand;
import io.datatok.djobi.utils.MyMapUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

class DumpPipelineCommandTest {

    @Inject
    private CommandFactory commandFactory;

    @Inject
    private DumpPipelineCommand dumpPipelineCommand;

    private final ByteArrayOutputStream myOut = new ByteArrayOutputStream();

    @BeforeEach void setup() {
        System.setOut(new PrintStream(myOut));
    }

    @Test void basic() {
        new CommandLine(dumpPipelineCommand).parse(new String[]{"--args", "date=today", "-Ahello=toto", "./src/test/resources/pipelines/good.yml"});

        Assertions.assertEquals(MyMapUtils.map("date", "today", "hello", "toto"), dumpPipelineCommand.args);
    }

    @Test void jsonExample() throws Exception {
        run(new String[]{"dump", "--format", "json", "pipeline", "--args", "date=today", "./src/test/resources/pipelines/good.yml"});

        final String output = captureStdout();

        Assertions.assertFalse(output.isEmpty());
        Assertions.assertNotNull(new ObjectMapper().readValue(output, HashMap.class));
    }

    @Test void drawExample() {
        run(new String[]{"dump", "--format", "plain", "pipeline", "--args", "date=today", "./src/test/resources/pipelines/good.yml"});

        final String output = captureStdout();

        Assertions.assertFalse(output.isEmpty());
    }

    private void run(final String[] args) {
        commandFactory.run(args);
    }

    private String captureStdout() {
        return myOut.toString();
    }

}
