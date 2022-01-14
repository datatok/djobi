package io.datatok.djobi.spark.actions.output;

import io.datatok.djobi.engine.actions.fs.output.FSOutputType;
import io.datatok.djobi.engine.enums.ExecutionStatus;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.spark.executor.SparkExecutor;
import io.datatok.djobi.test.ActionTest;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.io.IOFileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class SparkFSOutputTest extends ActionTest {

    @Test
    public void testRunner() throws Exception {

        final Path tempPath = Files.createTempDirectory("");
        final SparkExecutor executor = getSparkExecutor();

        executor.connect();

        ActionRunResult runResult = stageTestUtils.builder()
                .attachExecutor(SparkExecutor.TYPE)
                .addStageType(FSOutputType.TYPE)
                .withData(new SparkDataframe(buildDataset(5)))
                .configure(new Bag(
                        "format", "csv",
                        "mode", "overwrite",
                        "path", tempPath.toAbsolutePath().toString(),
                        "options", new Bag(
                            "header", "true",
                            "quoteMode", "all",
                            "quoteAll", "true"
                        )
                ))
                .run();

        Assertions.assertEquals(ExecutionStatus.DONE_OK, runResult.getStatus());

        boolean partFileFound = false;

        for (File partFile : Objects.requireNonNull(tempPath.toFile().listFiles()))
        {
            if (partFile.getName().startsWith("part-00"))
            {
                String[] lines = IOFileUtils.getContent(partFile.getAbsolutePath()).split("\n");

                Assertions.assertEquals("\"d\",\"d2\",\"i\",\"s\"", lines[0], String.format("Compare file %s", partFile.getAbsolutePath()));

                partFileFound = true;
            }
        }

        Assertions.assertTrue(partFileFound);
    }


}
