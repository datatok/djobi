package io.datatok.djobi.spark.actions.output;

import com.google.inject.Inject;
import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.check.CheckStatus;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.spark.executor.SparkExecutor;
import io.datatok.djobi.test.ActionTest;
import io.datatok.djobi.test.StageTestUtils;
import io.datatok.djobi.utils.Bag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SparkHiveOutputPostCheckerTest extends ActionTest {

    @Inject
    SparkHiveOutputPostChecker checker;

    @Test
    void testWrong() throws Exception {
        final Stage stage = StageTestUtils.getNewStage();

        stage.getJob().getPipeline().setExecutor(getSparkExecutor());

        stage
            .setParameters(ActionConfiguration.get(SparkHiveOutputConfig.class, new Bag("table", "_not_exists"), stage, templateUtils))
        ;

        CheckResult res = checker.postCheck(stage);

        Assertions.assertEquals(CheckStatus.DONE_ERROR, res.getStatus());
    }

    @Test
    void testSuccess() throws Exception {
        final Stage stage = StageTestUtils.getNewStage();
        final SparkExecutor executor = getSparkExecutor();

        executor.connect();

        executor.getSQLContext().read().json("./src/test/resources/data/json_1/json.txt").createOrReplaceTempView("sample");

        stage.getJob().getPipeline().setExecutor(executor);

        stage
            .setParameters(ActionConfiguration.get(SparkHiveOutputConfig.class, new Bag("table", "sample", "check_query", "SELECT COUNT(*) FROM sample WHERE title = \"Tom\""), stage, templateUtils))
        ;

        CheckResult res = checker.postCheck(stage);

        Assertions.assertEquals(CheckStatus.DONE_OK, res.getStatus());
        Assertions.assertEquals("1", res.getMeta().get("value").toString());
    }

}
