package io.datatok.djobi.spark.actions.generator;

import com.google.inject.Inject;
import io.datatok.djobi.engine.actions.utils.generator.RandomGeneratorConfig;
import io.datatok.djobi.engine.actions.utils.generator.beans.People;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.spark.executor.SparkExecutor;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SparkGeneratorRunner implements ActionRunner {

    private RandomGeneratorConfig config;

    @Inject
    private SparkExecutor sparkExecutor;

    @Override
    public ActionRunResult run(Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        this.config = (RandomGeneratorConfig) stage.getParameters();

        final Dataset<Row> data = runOnSpark(sparkExecutor, this.config.count);

        return ActionRunResult.success(new SparkDataframe(data));
    }

    static public Dataset<Row> runOnSpark(final SparkExecutor executor, int count) throws IOException {
        final SQLContext sqlContext = executor.getSQLContext();
        final List<People> beans = new ArrayList<>();

        RandomDataGenerator generator = new RandomDataGenerator();

        for (int i = 0; i < count; i++) {
            final People bean = new People();

            bean
                .setFirstName(generator.nextHexString(generator.nextInt(4, 16)))
                .setLastName(generator.nextHexString(generator.nextInt(4, 16)))
                .setAge(generator.nextInt(10, 100))
                .setSexe(i % 2 == 0 ? "F" : "M");
            ;

            beans.add(bean);
        }

        return sqlContext.createDataFrame(beans, People.class);
    }
}
