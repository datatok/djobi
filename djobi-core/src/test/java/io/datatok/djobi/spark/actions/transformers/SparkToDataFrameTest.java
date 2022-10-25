package io.datatok.djobi.spark.actions.transformers;

import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.data.StageDataList;
import io.datatok.djobi.engine.data.StageDataString;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.test.ActionTest;
import io.datatok.djobi.test.StageTestUtils;
import io.datatok.djobi.utils.Bag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Provider;
import java.util.Arrays;
import java.util.List;

public class SparkToDataFrameTest extends ActionTest {

    @javax.inject.Inject
    Provider<SparkToDataFrame.Runner> runnerProvider;

    @Test
    void testSimpleSuccess() throws Exception {
        List<String> strings = Arrays.asList(
            "{\"letter\" : \"A\", \"count\" : 12}",
            "{\"letter\" : \"B\", \"count\" : 1}",
            "{\"letter\" : \"C\", \"count\" : 2}"
        );

        ActionRunResult runResult = run(new Bag(), new StageDataList<>(strings));

        Assertions.assertTrue(runResult.getData() instanceof SparkDataframe);
        Assertions.assertEquals(3, ((SparkDataframe) runResult.getData()).getData().count());
    }

    @Test
    void testAliasTableSuccess() throws Exception {
        List<String> strings = Arrays.asList(
                "{\"letter\" : \"A\", \"count\" : 12}",
                "{\"letter\" : \"B\", \"count\" : 1}",
                "{\"letter\" : \"C\", \"count\" : 2}"
        );

        ActionRunResult runResult = run(new Bag("as_table", "my_letters"), new StageDataList<>(strings));

        Assertions.assertEquals(3, (long) getSparkExecutor().getSQLContext().sql("SELECT count(*) FROM my_letters").collectAsList().get(0).get(0));
    }

    @Test
    void testJSONArray() throws Exception {
        String jj = "[{\"letter\" : \"A\", \"count\" : 12},{\"letter\" : \"B\", \"count\" : 1}, {\"letter\" : \"C\", \"count\" : 2}]";

        ActionRunResult runResult = run(new Bag("format", SparkToDataFrame.FORMAT_JSON), new StageDataString(jj));

        Assertions.assertTrue(runResult.getData() instanceof SparkDataframe);
        Assertions.assertEquals(3, ((SparkDataframe)runResult.getData()).getData().count());
    }

    private ActionRunResult run(Bag args, StageData<?> data) throws Exception {
        final Stage stage = StageTestUtils.getNewStage();

        stage.setParameters(new SparkToDataFrame.Config(args, null, templateUtils));

        if (getSparkExecutor().isConnected()) {
            stage.getJob().getWorkflow().setExecutor(getSparkExecutor());
        }

        return runnerProvider.get().run(stage, data, getSparkExecutor());
    }

}
