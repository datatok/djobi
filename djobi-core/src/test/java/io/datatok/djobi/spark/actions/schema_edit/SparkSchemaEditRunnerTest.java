package io.datatok.djobi.spark.actions.schema_edit;

import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.spark.actions.schema_flatr.SparkSchemaFlatrType;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.test.ActionTest;
import io.datatok.djobi.test.MyTestRunner;
import io.datatok.djobi.utils.Bag;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;

@ExtendWith(MyTestRunner.class)
public class SparkSchemaEditRunnerTest extends ActionTest {

    @Test
    public void testSimple() throws Exception {
        getSparkExecutor().connect();

        Dataset<Row> df = getSparkExecutor().getSQLContext().read().json("./src/test/resources/data_json_2");

        ActionRunResult runResult = stageTestUtils.builder()
                .withData(new SparkDataframe(df))
                .attachExecutor(getSparkExecutor())
                .addStageType(SparkSchemaFlatrType.TYPE)
                .configure(new Bag("sort_column", "i", "sort_direction", "desc"))
                .run();

        Dataset<Row> newDF = ((SparkDataframe)runResult.getData()).getData();

        Assertions.assertNotNull(Arrays.stream(newDF.schema().fields()).filter(f -> f.name().equals("id")));
        Assertions.assertNotNull(Arrays.stream(newDF.schema().fields()).filter(f -> f.name().equals("user_agent_os")));
        Assertions.assertNotNull(Arrays.stream(newDF.schema().fields()).filter(f -> f.name().equals("user_agent_browser")));

        Assertions.assertEquals("MacOS", newDF.select("user_agent_os").first().get(0));
    }

}
