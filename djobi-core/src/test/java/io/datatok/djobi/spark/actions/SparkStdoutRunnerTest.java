package io.datatok.djobi.spark.actions;

import com.google.inject.Inject;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.enums.ExecutionStatus;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.spark.actions.generator.SparkGeneratorRunner;
import io.datatok.djobi.spark.actions.stdout.SparkStdoutRunner;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.test.ActionTest;
import io.datatok.djobi.test.StageTestUtils;
import io.datatok.djobi.utils.Bag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Provider;

public class SparkStdoutRunnerTest extends ActionTest {

    @Inject
    Provider<SparkStdoutRunner> runnerProvider;

    @Test
    void testSimpleSuccess() throws Exception {
        getSparkExecutor().connect();

        ActionRunResult runResult = run(new Bag(), new SparkDataframe(SparkGeneratorRunner.runOnSpark(getSparkExecutor(), 10)));

        Assertions.assertEquals(runResult.getStatus(), ExecutionStatus.DONE_OK);
    }

    private ActionRunResult run(Bag args, StageData<?> data) throws Exception {
        final Stage stage = StageTestUtils.getNewStage();

        //stage.setParameters(new Stdout.Config(args, null, templateUtils));

        if (getSparkExecutor().isConnected()) {
            stage.getJob().getWorkflow().setExecutor(getSparkExecutor());
        }

        return runnerProvider.get().run(stage, data, getSparkExecutor());
    }
}
