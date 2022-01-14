package io.datatok.djobi.engine.actions.fs.output;

import com.google.inject.Inject;
import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.check.CheckStatus;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.test.StageTestUtils;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FSOutputPostCheckerTest {

    static Path TEST_FILE_PATH = Paths.get("/tmp/toto.txt");

    @Inject
    private FSOutputPostChecker checker;

    @Inject
    private TemplateUtils templateUtils;

    @BeforeEach
    @AfterEach
    void before() {
        try {
            Files.deleteIfExists(TEST_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testWrong() throws Exception {
        Stage stage = StageTestUtils.getNewStage();

        stage.setParameters(ActionConfiguration.get(FSOutputConfig.class, new Bag("path", "file://" + TEST_FILE_PATH.toString()), stage, templateUtils));

        CheckResult res = checker.postCheck(stage);

        Assertions.assertEquals(CheckStatus.DONE_ERROR, res.getStatus());
        Assertions.assertEquals("File not found", res.getMeta("reason"));
    }

    @Test
    void testTooSmall() throws Exception {
        Stage stage = StageTestUtils.getNewStage();

        Files.write(TEST_FILE_PATH, "dfkdsjfkjds".getBytes());

        stage.setParameters(ActionConfiguration.get(FSOutputConfig.class, new Bag("path", "file://" + TEST_FILE_PATH.toString()), stage, templateUtils));

        CheckResult res = checker.postCheck(stage);

        Assertions.assertEquals(CheckStatus.DONE_ERROR, res.getStatus());
        Assertions.assertEquals("File exists, but too small", res.getMeta("reason"));
    }

    @Test
    void testOk() throws Exception {
        Stage stage = StageTestUtils.getNewStage();

        Files.write(TEST_FILE_PATH, "dfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjdsdfkdsjfkjds".getBytes());

        stage.setParameters(ActionConfiguration.get(FSOutputConfig.class, new Bag("path", "file://" + TEST_FILE_PATH.toString()), stage, templateUtils));

        CheckResult res = checker.postCheck(stage);

        Assertions.assertEquals(CheckStatus.DONE_OK, res.getStatus());
    }
}
