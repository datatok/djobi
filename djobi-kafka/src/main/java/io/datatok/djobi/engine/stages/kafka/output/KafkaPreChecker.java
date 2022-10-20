package io.datatok.djobi.engine.stages.kafka.output;

import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionPreChecker;
import io.datatok.djobi.utils.PropertiesUtils;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class KafkaPreChecker implements ActionPreChecker {

    private static final Logger logger = Logger.getLogger(KafkaPreChecker.class);

    static public final String REASON_OK_TOPIC_CREATED = "endpoint ok, topic created";

    static public final String REASON_OK_TOPIC_FOUND = "endpoint ok, topic exist";

    static public final String REASON_ERROR_NO_ENDPOINT = "endpoint issue";

    static public final String REASON_ERROR_TOPIC_NOT_FOUND = "endpoint ok, topic not found";

    @Override
    public CheckResult preCheck(Stage stage) {
        final KafkaOutputConfig config = (KafkaOutputConfig) stage.getParameters();

        logger.debug(String.format("Checking Kafka server at %s", config.endpoints.toString()));

        Properties props = PropertiesUtils.build(
            "bootstrap.servers", config.endpoints,
            "fetch.max.wait.ms", 1000,
            "heartbeat.interval.ms", 1500,
            "session.timeout.ms", 2000,
            "request.timeout.ms", 3000
        );

        try (Admin kafkaAdminClient = Admin.create(props)) {
            final Set<String> existingTopics = kafkaAdminClient.listTopics().names().get();

            final boolean topicExists = existingTopics.contains(config.topic.name);

            logger.debug(String.format("Found %d topic(s)", existingTopics.size()));

            if (topicExists) {
                logger.debug(String.format("Topic [%s] exists", config.topic.name));
                return CheckResult.ok(CheckResult.REASON, REASON_OK_TOPIC_FOUND);
            }

            logger.debug(String.format("Topic [%s] does not exist", config.topic.name));

            if (config.topic.create) {
                logger.debug(String.format("Creating topic [%s]", config.topic.name));

                NewTopic newTopic = new NewTopic(config.topic.name, config.topic.partitions, config.topic.replicationFactor);

                kafkaAdminClient.createTopics(List.of(newTopic));

                logger.debug(String.format("Topic [%s] created", config.topic.name));

                return CheckResult.ok(CheckResult.REASON, REASON_OK_TOPIC_CREATED);
            }

        } catch (ExecutionException | InterruptedException e) {
            return CheckResult.error(e.getMessage());
        }

        return CheckResult.error(REASON_ERROR_TOPIC_NOT_FOUND);
    }
}
