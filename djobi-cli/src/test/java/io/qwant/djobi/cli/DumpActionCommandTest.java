package io.datatok.djobi.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

class DumpActionCommandTest {

    @Inject
    private CommandFactory commandFactory;

    @Test void jsonExample() throws Exception {
        run(new String[]{"dump", "--format", "json", "action"});
    }

    @Test void tableExample() {
        run(new String[]{"dump", "action"});
    }

    private void run(final String[] args) {
        commandFactory.run(args);
    }

}
