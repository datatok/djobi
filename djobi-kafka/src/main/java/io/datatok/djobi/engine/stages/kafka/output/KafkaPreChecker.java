package io.datatok.djobi.engine.stages.kafka.output;

import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionPreChecker;
import io.datatok.djobi.utils.PropertiesUtils;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class KafkaPreChecker implements ActionPreChecker {

    private static Logger logger = Logger.getLogger(KafkaPreChecker.class);

    static public final String REASON_OK_NO_TOPIC = "connection ok, no topic test";

    static public final String REASON_OK_TOPIC_FOUND = "topic found";

    static public final String REASON_ERROR_TOPIC_NOT_FOUND = "topic not found";

    @Override
    public CheckResult preCheck(Stage stage) {
        final KafkaOutputConfig config = (KafkaOutputConfig) stage.getParameters();

        logger.debug(String.format("Checking Kafka server at %s", config.servers.toString()));

        Properties props = PropertiesUtils.build(
                "bootstrap.servers", config.servers,
                "fetch.max.wait.ms", 1000,
                "heartbeat.interval.ms", 1500,
                "session.timeout.ms", 2000,
                "request.timeout.ms", 3000,
                "key.deserializer", StringDeserializer.class.getName(),
                "value.deserializer", StringDeserializer.class.getName()
        );

        Map<String, List<PartitionInfo>> topics;

        try {
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            topics = consumer.listTopics();

            //Object m = consumer.metrics();

            //consumer.subscribe(Collections.singletonList("djobi"));

            consumer.close();
        } catch (KafkaException e) {
            return CheckResult.error(e.getMessage());
        }

        if (config.topic == null || config.topic.isEmpty()) {
            return CheckResult.ok(CheckResult.REASON, REASON_OK_NO_TOPIC);
        }

        if (topics.containsKey(config.topic)) {
            return CheckResult.ok(CheckResult.REASON, REASON_OK_TOPIC_FOUND);
        }

        return CheckResult.error(REASON_ERROR_TOPIC_NOT_FOUND);
    }
}
