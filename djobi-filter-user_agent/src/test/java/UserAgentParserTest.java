import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.spark.executor.SparkExecutor;
import io.datatok.djobi.test.MyTestRunner;
import io.datatok.djobi.test.StageTestUtils;
import io.datatok.djobi.user_agent.UserAgentParserConfig;
import io.datatok.djobi.user_agent.UserAgentParserRunner;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MyTestRunner.class)
public class UserAgentParserTest {

    @Inject
    SparkExecutor sparkExecutor;

    @Inject
    UserAgentParserRunner runner;

    @Inject
    TemplateUtils templateUtils;

    @Test
    void test() throws Exception {
        List<Row> list=new ArrayList<>();

        list.add(RowFactory.create(1, RowFactory.create("", "local")));
        list.add(RowFactory.create(2, RowFactory.create("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36", "web")));
        list.add(RowFactory.create(3, RowFactory.create(null, "local")));

        List<StructField> structFields = new ArrayList<>();

        structFields.add(DataTypes.createStructField("id", DataTypes.IntegerType, true));
        structFields.add(DataTypes.createStructField("client", DataTypes.createStructType(
                Arrays.asList(
                    DataTypes.createStructField("user_agent", DataTypes.StringType, true),
                    DataTypes.createStructField("from", DataTypes.StringType, true)
                )
        ), true));

        sparkExecutor.connect();

        Dataset<Row> df = sparkExecutor.getSQLContext().createDataFrame(list, DataTypes.createStructType(structFields));

        df.show(10, false);

        Stage stage = StageTestUtils.getNewStage();

        stage.getJob().getWorkflow().setExecutor(sparkExecutor);

        stage.setParameters(ActionConfiguration.get(UserAgentParserConfig.class, new Bag("source", "client.user_agent", "target", "client_browser", "fields", "fullname,os,major"), stage, templateUtils));

        ActionRunResult runResult = runner.run(stage, new SparkDataframe(df), sparkExecutor);

        Dataset<Row> newDf = ((SparkDataframe) runResult.getData()).getData();

        newDf.show(10, false);

        Assertions.assertEquals(3, newDf.count());

        //List<Row> rows = newDf.collectAsList();

        Assertions.assertEquals("Chrome", newDf.where("id = 2").select("client_browser.fullname").first().get(0));
        Assertions.assertEquals("Mac OS X", newDf.where("id = 2").select("client_browser.os").first().get(0));
        Assertions.assertEquals("75", newDf.where("id = 2").select("client_browser.major").first().get(0));
        Assertions.assertThrows(AnalysisException.class, () -> newDf.where("id = 2").select("client_browser.minor").first().get(0));
    }
}
