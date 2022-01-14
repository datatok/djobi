package io.datatok.djobi.engine.stages;

import com.google.inject.Inject;
import io.datatok.djobi.engine.actions.csv.CSVFilterConfigurator;
import io.datatok.djobi.engine.actions.sql.SQLConfigurator;
import io.datatok.djobi.engine.actions.utils.generator.RandomGeneratorType;
import io.datatok.djobi.engine.actions.utils.stdout.StdoutType;
import io.datatok.djobi.engine.flow.ExecutionContext;
import io.datatok.djobi.engine.stage.ActionFactory;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionConfigurator;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.executors.ExecutorPool;
import io.datatok.djobi.executors.LocalExecutor;
import io.datatok.djobi.spark.actions.generator.SparkGeneratorRunner;
import io.datatok.djobi.spark.actions.output.SparkHiveOutputConfigurator;
import io.datatok.djobi.spark.actions.output.SparkHiveOutputRunner;
import io.datatok.djobi.spark.actions.output.SparkHiveOutputType;
import io.datatok.djobi.spark.actions.stdout.SparkStdoutRunner;
import io.datatok.djobi.spark.actions.transformers.SparkCSVRunner;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.spark.executor.SparkExecutor;
import io.datatok.djobi.test.MyTestRunner;
import io.datatok.djobi.test.StageTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

@ExtendWith(MyTestRunner.class)
class ActionFactoryTest {

    @Inject
    private ActionFactory factory;

    @Inject
    private ExecutorPool executorPool;

    @Inject
    private StageTestUtils stageTestUtils;

    @Inject
    private ExecutionContext executionContext;

    @Test
    void stageProviderTest() throws IOException {

        /*executionContext.setExecutor( executorPool.get(LocalExecutor.TYPE));

        Assertions.assertSame(factory.getRunner(
            stageTestUtils.builder()
                .addStageType(StdoutType.NAME)
                .getStage()
        ).getClass(), StdoutRunner.class);*/

        SparkExecutor executor = (SparkExecutor) executorPool.get(SparkExecutor.TYPE);

        executionContext.setExecutor(executor);

        // we have a Spark DataFrame as job data > generator must be SparkStdoutRunner
        Assertions.assertSame(SparkStdoutRunner.class, factory.getRunner(
            stageTestUtils.builder()
                .attachExecutor(executor)
                .addStageType(StdoutType.NAME)
                .withData(new SparkDataframe(SparkGeneratorRunner.runOnSpark(executor, 10)))
                .getStage()
        ).getClass());

        // we have a Spark executor > generator must be SparkGeneratorRunner
        Assertions.assertSame(factory.getRunner(
            stageTestUtils.builder()
                .attachExecutor(executor)
                .addStageType(RandomGeneratorType.NAME)
                .getStage()
        ).getClass(), SparkGeneratorRunner.class);
    }

    @Test()
    void genericOverrideFallbackTest()
    {
        Assertions.assertTrue(factory.getConfigurator(
            stageTestUtils
                .builder()
                .attachExecutor(SparkExecutor.TYPE)
                .addStageType("org.spark.sql")
                .getStage()
        ) instanceof SQLConfigurator);
    }

    @Test()
    void missingServiceTest()
    {
        Assertions.assertNull(factory.getRunner(new Stage()
        {{
            setKind("not exists type");
        }}));
    }

    @Test()
    void getCSVActionTest()
    {
        executionContext.setExecutor(executorPool.get(LocalExecutor.TYPE));

        // No default CSV runner
        ActionRunner runner = factory.getRunner(new Stage()
        {{
            setKind("csv");
        }});

        Assertions.assertNull(runner);

        // Generic configurator
        ActionConfigurator configurator = factory.getConfigurator(new Stage()
        {{
            setKind("csv");
        }});

        Assertions.assertSame(configurator.getClass(), CSVFilterConfigurator.class);

        executionContext.setExecutor(executorPool.get(SparkExecutor.TYPE));

        // With Spark as executor
        runner = factory.getRunner(
            stageTestUtils
                .builder()
                .attachExecutor(SparkExecutor.TYPE)
                .addStageType("csv")
                .getStage()
        );

        Assertions.assertSame(runner.getClass(), SparkCSVRunner.class);
    }

    @Test()
    void testNewFormat()
    {
        final ActionRunner runner = factory.getRunner(new Stage()
        {{
            setKind(SparkHiveOutputType.TYPE);
        }});

        Assertions.assertSame(runner.getClass(), SparkHiveOutputRunner.class);
    }

    @Test()
    void testConfigurator()
    {
        final ActionConfigurator runner = factory.getConfigurator(new Stage()
        {{
            setKind(SparkHiveOutputType.TYPE);
        }});

        Assertions.assertSame(runner.getClass(), SparkHiveOutputConfigurator.class);
    }
}
