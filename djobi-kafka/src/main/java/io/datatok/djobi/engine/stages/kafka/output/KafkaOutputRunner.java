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
import io.datatok.djobi.spark.executor.SparkExecutor;
import io.datatok.djobi.utils.PropertiesUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.Logger;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.util.LongAccumulator;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class KafkaOutputRunner implements ActionRunner {

    private static Logger logger = Logger.getLogger(KafkaOutputRunner.class);

    public Boolean accept(final Job job) {
       /* if ( ((DataHolder) job.getData()).getType().equals(SerializedData.TYPE)) {
            return true;
        }*/

        return false;
    }

    @Override
    public ActionRunResult run(Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        final Job job = stage.getJob();
        final KafkaOutputConfig config = (KafkaOutputConfig) stage.getParameters();

        logger.debug(String.format("[config] %s", config.toString()));

        Properties kafkaProperties = PropertiesUtils.build(
    "bootstrap.servers", config.servers,
            "acks", "all",
            "retries", 3,
            "batch.size", config.batchSize,
            "linger.ms", 1,
            "request.timeout.ms", 5000,
            "buffer.memory", config.bufferSize,
            "key.serializer", StringSerializer.class.getName(),
            "value.serializer", StringSerializer.class.getName()
        );

        if (config.kafkaOptions != null) {
            kafkaProperties.putAll(config.kafkaOptions);
        }

        final KafkaProducer<Object, Object> producer = new KafkaProducer<>(kafkaProperties);

        if (contextData.getKind().getType().equals(SparkDataKind.TYPE_DATASET))
        {
            final SparkContext sparkContext = ((SparkExecutor)job.getPipeline().getExecutor()).getSparkContext();
            final LongAccumulator messages = sparkContext.longAccumulator("messages");

            logger.info("DataFrame detected, running producer for each partition");

            ((Dataset<?>) contextData.getData()).foreachPartition(new KafkaProducerFunction<>(messages, kafkaProperties, config.topic));

            reportPostCheck(stage, messages.value());
        }
        else if (contextData.getKind().getType().equals(SparkDataKind.TYPE_JAVA_RDD))
        {
            throw new StageException(stage, "not working with RDD since Spark 3!");
            /*final SparkContext sparkContext = ((SparkExecutor)job.getPipeline().getExecutor()).getSparkContext();
            final LongAccumulator messages = sparkContext.longAccumulator("messages");
            final JavaRDD<String> rdds = ((JavaRDD<String>) contextData.getData());

            logger.info(String.format("spark RDD: %d partitions", rdds.getNumPartitions()));

            rdds.foreachPartition(new KafkaProducerFunction<String>(messages, kafkaProperties, config.topic));

            reportPostCheck(stage, messages.value());*/
        }
        else if (contextData instanceof StageDataListString)
        {
            int i = 0;

            for (String item : ((StageDataListString) contextData).getData()) {
                producer.send(new ProducerRecord<>(config.topic, item)).get();
                i++;
            }

            reportPostCheck(stage, i);

            producer.close();
        }
        else if (contextData instanceof StageDataString)
        {
            producer.send(new ProducerRecord<>(config.topic, ((StageDataString) contextData).getData())).get(5, TimeUnit.SECONDS);

            producer.flush();

            reportPostCheck(stage, 1);
        }

        return ActionRunResult.success();
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
