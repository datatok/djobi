package io.datatok.djobi.spark.actions;

import io.datatok.djobi.engine.actions.utils.generator.RandomGeneratorType;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.spark.executor.SparkExecutor;
import io.datatok.djobi.test.MyTestRunner;
import io.datatok.djobi.test.StageTestUtils;
import io.datatok.djobi.utils.Bag;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

@ExtendWith(MyTestRunner.class)
public class RandomGeneratorTest {

    @Inject
    private StageTestUtils stageTestUtils;

    @Test
    public void testRunner() throws Exception {
        ActionRunResult runResult = stageTestUtils.builder()
                .attachExecutor(SparkExecutor.TYPE)
                .addStageType(RandomGeneratorType.NAME)
                .configure(new Bag(
                        "count", 10
                ))
            .run();

       final Dataset<Row> df = ((SparkDataframe) runResult.getData()).getData();

       Assertions.assertEquals(10, df.count());
    }

}
