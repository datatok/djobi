package io.datatok.djobi.engine.stages.elasticsearch.output;

import io.datatok.djobi.configuration.Configuration;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.exceptions.StageException;
import io.datatok.djobi.spark.actions.generator.SparkGeneratorRunner;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.spark.executor.SparkExecutor;
import io.datatok.djobi.test.ActionTest;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.elasticsearch.ElasticsearchUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.inject.Inject;
import java.io.IOException;

@Tag("IntegrationTest")
class ESOutputRunnerTest extends ActionTest {

    @Inject
    private ElasticsearchUtils elasticsearchUtils;

    @Inject
    private Configuration configuration;

    @BeforeEach
    void resetES() throws IOException {
        elasticsearchUtils.deleteIndex(configuration.getString("elasticsearch"), "test");
    }

/*
    @Test void checkPositive() throws Exception {
        ESOutputRunner runner = diManager.get(ESOutputRunner.class);
        boolean check = runner.setStage(new Stage(new Bag(
                "host", "http://localhost:9200",
                "clean_query", "* OR 1=1"
        ))).check(new Job());

        Assertions.assertTrue(check);

        Field field = ESOutputRunner.class.getDeclaredField("elasticsearchServerVersion");
        field.setAccessible(true);

        Assertions.assertEquals(ElasticsearchMockClient.version, field.get(runner).toString());
    }

    @Test void checkNegative() throws Exception {
        ESOutputRunner runner = diManager.get(ESOutputRunner.class);
        boolean check = runner.setStage(new Stage(new Bag(
                "host", "http://localhost:92",
                "clean_query", "* OR 1=1"
        ))).check(new Job());

        Assertions.assertFalse(check);
    }
*/
    @Test
    void runWithoutData() throws Exception {
        Assertions.assertThrows(StageException.class, () -> stageTestUtils.run(ESOutputType.TYPE, new Bag(
                        "host", configuration.getString("elasticsearch"),
                        "clean_query", "* OR 1=1"
                ))
        );
    }

    @Test
    void testWithoutCleaning() throws Exception {
        run(null);

        Assertions.assertEquals(10, elasticsearchUtils.searchCount(configuration.getString("elasticsearch"), "test", null));
    }

    @Test
    void testCleanExistingData() throws Exception {
        final String esURL = configuration.getString("elasticsearch");

        run(null);

        Assertions.assertEquals(10, elasticsearchUtils.searchCount(esURL, "test", null));

        // Run again, no cleaning
        run(null);

        Assertions.assertEquals(20, elasticsearchUtils.searchCount(esURL, "test", null));

        // Run again, with cleaning
        run("*");

        Assertions.assertEquals(10, elasticsearchUtils.searchCount(esURL, "test", null));

        // Run again, with half-cleaning
        run("sexe:F");

        Assertions.assertEquals(15, elasticsearchUtils.searchCount(esURL, "test", null));
    }

    private ActionRunResult run(String cleanQuery) throws Exception {
        final String esURL = configuration.getString("elasticsearch");

        SparkExecutor executor = getSparkExecutor();

        executor.connect();

        Dataset<Row> df = SparkGeneratorRunner.runOnSpark(executor, 10);
        SparkDataframe dfData = new SparkDataframe(df);

        ActionRunResult runResult =
            stageTestUtils
                .builder()
                    .addStageType(ESOutputType.TYPE)
                    .configure(new Bag(
                        "host", esURL,
                        "clean_query", cleanQuery,
                        "index", "test/doc"
                    ))
                    .run(dfData);

        elasticsearchUtils.refresh(esURL, "test");

        return runResult;
    }

}
