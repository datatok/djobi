package io.datatok.djobi.engine.stages.kafka.output;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.data.StageDataListString;
import io.datatok.djobi.engine.data.StageDataString;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.exceptions.StageException;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.data.SparkDataKind;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.spark.executor.SparkExecutor;
import io.datatok.djobi.utils.PropertiesUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.Logger;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.util.LongAccumulator;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class KafkaOutputRunner implements ActionRunner {

    private static final Logger logger = Logger.getLogger(KafkaOutputRunner.class);

    private KafkaOutputConfig config;

    @Override
    public ActionRunResult run(Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        this.config = (KafkaOutputConfig) stage.getParameters();

        logger.debug(String.format("[config] %s", config.toString()));

        if (contextData.getKind().getType().equals(SparkDataKind.TYPE_DATASET))
        {
            writeToFS(((SparkDataframe) contextData).getData());

            //reportPostCheck(stage, messages.value());
        }
        else
        {
            throw new StageException(stage, "Must be a DataSet!");
        }

        return ActionRunResult.success();
    }

    protected void writeToFS(final Dataset<?> df) {
        logger.info("Writing DataFrame");

        final Map<String, String> options = new HashMap<>();

        if (config.topic.name != null && !config.topic.name.isEmpty()) {
            options.put("topic", config.topic.name);
        }

        options.put("kafka.bootstrap.servers", String.join(",", config.endpoints));

        options.putAll(config.kafkaOptions);

        df
            .write()
            .options(options)
            .format("kafka")
            .save()
        ;
    }

    private void reportPostCheck(Stage stage, Long count) {
        stage.setPostCheck(
            CheckResult.ok(
                "display", count + " messages",
                "value", count,
                "unit", "messages"
            )
        );
    }

    private void reportPostCheck(Stage stage, int count) {
        reportPostCheck(stage, new Long(count));
    }
}
