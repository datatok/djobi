package io.datatok.djobi.engine.stages.kafka;

import io.datatok.djobi.engine.check.CheckStatus;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.data.StageDataString;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stages.kafka.output.KafkaOutputConfig;
import io.datatok.djobi.engine.stages.kafka.output.KafkaOutputRunner;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.spark.executor.SparkExecutor;
import io.datatok.djobi.test.MyTestRunner;
import io.datatok.djobi.test.StageTestUtils;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import javax.inject.Provider;

@ExtendWith(MyTestRunner.class)
@Tag("IntegrationTest")
public class KafkaOutputRunnerTest {

    @Inject
    Provider<KafkaOutputRunner> runnerProvider;

    @Inject
    private TemplateUtils templateUtils;

    @Inject
    private SparkExecutor sparkExecutor;

    private String kafkaServer = null;

    @BeforeEach
    void init() {
        if (kafkaServer == null) {
            kafkaServer = "kafka1:9092";
        }
    }

    @BeforeAll
    static void beforeAll() {
        Logger.getLogger(KafkaProducer.class).setLevel(Level.toLevel("TRACE"));
    }

    @Test void testWithSingleStringMessage() throws Exception {
        run(
            new StageDataString("Hello, il fait super beau, allons vite à la plage boire du rosé !"),
            new Bag(
                "topic", "djobi",
                "kafka", new Bag(
            "max.block.ms", 2000,
                    "buffer.memory", 1024,
                    "batch.size", 0,
                    "linger.ms", 5,
                    "metadata.fetch.timeout.ms", 100,
                    "acks", "1"
                ),
                "servers", kafkaServer
            )
        );
    }

    @Test void testWithSparkDataFrame() throws Exception {

        sparkExecutor.connect();

        final Dataset<Row> df = sparkExecutor.getSQLContext().read().json("./src/test/resources/json_1");

        Stage stage = run(
                new SparkDataframe(df),
                new Bag(
                        "topic", "djobi",
                        "kafka", new Bag(
                        "max.block.ms", 2000,
                        "buffer.memory", 1024,
                        "batch.size", 0,
                        "linger.ms", 5,
                        "metadata.fetch.timeout.ms", 100,
                        "acks", "1"
                ),
                        "servers", kafkaServer
                )
        );

        Assertions.assertEquals(CheckStatus.DONE_OK, stage.getPostCheck().getStatus());
        Assertions.assertEquals("3", stage.getPostCheck().getMeta("value").toString());
    }

    private Stage run(StageData<?> stageData, Bag args) throws Exception {
        final Stage stage = StageTestUtils.getNewStage();

        stage.setParameters(new KafkaOutputConfig(args, null, templateUtils));

        if (sparkExecutor.isConnected()) {
            stage.getJob().getWorkflow().setExecutor(sparkExecutor);
        }

        runnerProvider.get().run(stage, stageData, sparkExecutor);

        return stage;
    }
}
