package io.datatok.djobi.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

class DumpEnvCommandTest {

    @Inject
    private CommandKernel commandKernel;

    private final ByteArrayOutputStream myOut = new ByteArrayOutputStream();

    @BeforeEach void setup() {
        System.setOut(new PrintStream(myOut));
    }

    @Test void jsonExample() throws Exception {
        run("dump", "--format", "json", "env");

        final String output = captureStdout();

        Assertions.assertFalse(output.isEmpty());
        Assertions.assertNotNull(new ObjectMapper().readValue(output, HashMap.class));
    }

    @Test void tableExample() {
        run("dump", "--format", "plain", "env");

        final String output = captureStdout();

        Assertions.assertFalse(output.isEmpty());
    }

    @Test void tableWithGrepExample() {
        run("dump", "--format", "plain", "--grep", "hello,djobi", "env");

        final String output = captureStdout();

        Assertions.assertFalse(output.isEmpty());
        Assertions.assertTrue(output.contains("djobi.hello"));
        Assertions.assertTrue(output.split("\n").length < 5);
    }

    private void run(String... args) {
        commandKernel.getRootCommand().execute(args);
    }

    private String captureStdout() {
        return myOut.toString();
    }

}
