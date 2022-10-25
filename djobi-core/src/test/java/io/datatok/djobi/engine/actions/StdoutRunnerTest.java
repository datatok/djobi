package io.datatok.djobi.engine.actions;

import io.datatok.djobi.engine.Workflow;
import io.datatok.djobi.plugins.report.Reporter;
import io.datatok.djobi.test.ActionTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;


public class StdoutRunnerTest extends ActionTest {

    @Inject
    private Reporter reporter;

    @Test
    public void testRunner() throws Exception {
        final Workflow workflow = getPipeline("stdout.yml");

        Assertions.assertEquals(3, workflow.getJobs().get(0).getStages().size());

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(baos);

        reporter.setPrintStream(out);

        engine.run(workflow, "raw");

        Assertions.assertEquals(
    "{id=1, title=Tom}\n{id=2, title=Alicia}\n{id=3, title=Joseph}\n",
            baos.toString(StandardCharsets.UTF_8.name())
        );

        baos.reset();

        engine.run(workflow, "as_json");

        Assertions.assertEquals(
                "{\"id\":1,\"title\":\"Tom\"}\n" +
                        "{\"id\":2,\"title\":\"Alicia\"}\n" +
                        "{\"id\":3,\"title\":\"Joseph\"}\n",
                baos.toString(StandardCharsets.UTF_8.name())
        );

        baos.close();
    }

}
