package io.datatok.djobi.spark.actions;

import io.datatok.djobi.engine.actions.csv.CSVType;
import io.datatok.djobi.engine.actions.fs.input.FSInputType;
import io.datatok.djobi.engine.actions.fs.output.FSOutputType;
import io.datatok.djobi.engine.actions.json.JSONType;
import io.datatok.djobi.engine.actions.sql.SQLType;
import io.datatok.djobi.engine.actions.utils.generator.RandomGeneratorType;
import io.datatok.djobi.engine.actions.utils.stdout.StdoutType;
import io.datatok.djobi.engine.flow.ExecutionContext;
import io.datatok.djobi.engine.stage.ActionProvider;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.utils.StageCandidate;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.actions.collectors.ByWeekTransformer;
import io.datatok.djobi.spark.actions.collectors.CollectRunner;
import io.datatok.djobi.spark.actions.fs.SparkFSChecker;
import io.datatok.djobi.spark.actions.fs.SparkFSInput;
import io.datatok.djobi.spark.actions.fs.SparkFSOutput;
import io.datatok.djobi.spark.actions.generator.SparkGeneratorRunner;
import io.datatok.djobi.spark.actions.mutate.SparkMutateConfigurator;
import io.datatok.djobi.spark.actions.mutate.SparkMutateRunner;
import io.datatok.djobi.spark.actions.mutate.SparkMutateType;
import io.datatok.djobi.spark.actions.output.SparkHiveOutputConfigurator;
import io.datatok.djobi.spark.actions.output.SparkHiveOutputPostChecker;
import io.datatok.djobi.spark.actions.output.SparkHiveOutputRunner;
import io.datatok.djobi.spark.actions.output.SparkHiveOutputType;
import io.datatok.djobi.spark.actions.schema_flatr.SparkSchemaFlatrConfigurator;
import io.datatok.djobi.spark.actions.schema_flatr.SparkSchemaFlatrRunner;
import io.datatok.djobi.spark.actions.schema_flatr.SparkSchemaFlatrType;
import io.datatok.djobi.spark.actions.sql.SparkSQLPreChecker;
import io.datatok.djobi.spark.actions.sql.SparkSQLRunner;
import io.datatok.djobi.spark.actions.stdout.SparkStdoutRunner;
import io.datatok.djobi.spark.actions.transformers.SparkJSONRunner;
import io.datatok.djobi.spark.actions.transformers.SparkCSVRunner;
import io.datatok.djobi.spark.actions.transformers.SparkToDataFrame;
import io.datatok.djobi.spark.data.SparkDataKind;
import io.datatok.djobi.spark.executor.SparkExecutor;

import java.util.Arrays;
import java.util.List;

public class SparkActionProvider extends ActionProvider {

    static public final String NAME = "org.spark";

    static private final List<String> whitelistStageType = Arrays.asList(
        RandomGeneratorType.NAME,
        FSInputType.TYPE,
        SQLType.NAME
    );

    static private final List<String> whitelistIfDataIsOK = Arrays.asList(
        FSOutputType.TYPE,
        StdoutType.NAME,
        CSVType.NAME,
        JSONType.NAME
    );

    @Override
    public void configure() {
        registerConfigurator(SparkHiveOutputType.TYPE, SparkHiveOutputConfigurator.class);
        registerRunner(SparkHiveOutputType.TYPE, SparkHiveOutputRunner.class);
        registerPostChecker(SparkHiveOutputType.TYPE, SparkHiveOutputPostChecker.class);

        registerConfigurator(SparkToDataFrame.TYPE, SparkToDataFrame.Configurator.class);
        registerRunner(SparkToDataFrame.TYPE, SparkToDataFrame.Runner.class);

        registerConfigurator(SparkMutateType.TYPE, SparkMutateConfigurator.class);
        registerRunner(SparkMutateType.TYPE, SparkMutateRunner.class);

        registerConfigurator(SparkMutateType.TYPE_2, SparkMutateConfigurator.class);
        registerRunner(SparkMutateType.TYPE_2, SparkMutateRunner.class);

        registerConfigurator(SparkSchemaFlatrType.TYPE, SparkSchemaFlatrConfigurator.class);
        registerRunner(SparkSchemaFlatrType.TYPE, SparkSchemaFlatrRunner.class);

        registerRunner(ByWeekTransformer.TYPE, ByWeekTransformer.class);
        registerRunner(CollectRunner.TYPE, CollectRunner.class);

        // Add generic implementation runners
        registerRunner(SparkActionProvider.resolveFullname(StdoutType.NAME), SparkStdoutRunner.class);
        registerRunner(SparkActionProvider.resolveFullname(RandomGeneratorType.NAME), SparkGeneratorRunner.class);
        registerRunner(SparkActionProvider.resolveFullname(FSInputType.TYPE), SparkFSInput.class);
        registerRunner(SparkActionProvider.resolveFullname(FSOutputType.TYPE), SparkFSOutput.class);
        registerRunner(SparkActionProvider.resolveFullname(CSVType.NAME), SparkCSVRunner.class);
        registerRunner(SparkActionProvider.resolveFullname(JSONType.NAME), SparkJSONRunner.class);

        registerPreChecker(SparkActionProvider.resolveFullname(SQLType.NAME), SparkSQLPreChecker.class);
        registerRunner(SparkActionProvider.resolveFullname(SQLType.NAME), SparkSQLRunner.class);

        registerPostChecker(SparkActionProvider.resolveFullname(FSOutputType.TYPE), SparkFSChecker.class);

        registerProvider(SparkActionProvider.NAME, this);

        // Provide the Spark pipeline executor as default (@todo => must be defined via the engine!)
        this.bind(Executor.class).to(SparkExecutor.class);
    }

    @Override
    public StageCandidate getGenericActionCandidates(Stage stage, String phase, ExecutionContext executionContext) {
        final String stageType = stage.getKind();

        // Be sure we have a Spark executor and action exists.
        if (!(executionContext.getExecutor() instanceof SparkExecutor) || !hasAction(phase, resolveFullname(stageType))) {
            return null;
        }

        if (whitelistStageType.contains(stageType)) {
            return new StageCandidate(resolveFullname(stageType), 200);
        }

        if (whitelistIfDataIsOK.contains(stageType)) {
            int score = 1;

            if (executionContext.getStageData() != null &&
                executionContext.getStageData().getKind().getProvider().equals(SparkDataKind.GROUP))
            {
                score = 200;
            }

            return new StageCandidate(resolveFullname(stageType), score);
        }

        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static String resolveFullname(String name) {
        return NAME + "." + name;
    }

}
