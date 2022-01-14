package io.datatok.djobi.engine.stages.kafka;

import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.check.CheckStatus;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionPreChecker;
import io.datatok.djobi.engine.stages.kafka.output.KafkaOutputConfig;
import io.datatok.djobi.engine.stages.kafka.output.KafkaPreChecker;
import io.datatok.djobi.test.MyTestRunner;
import io.datatok.djobi.test.StageTestUtils;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import javax.inject.Provider;

@ExtendWith(MyTestRunner.class)
public class KafkaPreCheckerStage {

    @Inject
    Provider<KafkaPreChecker> preCheckerProvider;

    @Inject
    private TemplateUtils templateUtils;

    private String kafkaServer = null;

    @BeforeEach void init() {
        if (kafkaServer == null) {
            kafkaServer = "kafka1:9092";
        }
    }

    @Test void testWrongServer() throws Exception {
        CheckResult r = check(
        "topic", "djobi",
                "servers", "localhost:9091"
        );

        Assertions.assertEquals(CheckStatus.DONE_ERROR, r.getStatus());
    }

    @Test void testGoodServerWithoutTopic() throws Exception {
        CheckResult r = check(
                "servers", kafkaServer
        );

        Assertions.assertEquals(CheckStatus.DONE_OK, r.getStatus());
        Assertions.assertEquals(KafkaPreChecker.REASON_OK_NO_TOPIC, r.getMeta(CheckResult.REASON));
    }

    @Test void testGoodServerWithWrongTopic() throws Exception {
        CheckResult r = check(
                "topic", "djoba",
                "servers", kafkaServer
        );

        Assertions.assertEquals(CheckStatus.DONE_ERROR, r.getStatus());
        Assertions.assertEquals(KafkaPreChecker.REASON_ERROR_TOPIC_NOT_FOUND, r.getMeta(CheckResult.REASON));
    }

    @Test void testGoodServerWithGoodTopic() throws Exception {
        CheckResult r = check(
                "topic", "djobi",
                "servers", kafkaServer
        );

        Assertions.assertEquals(CheckStatus.DONE_OK, r.getStatus());
        Assertions.assertEquals(KafkaPreChecker.REASON_OK_TOPIC_FOUND, r.getMeta(CheckResult.REASON));
    }

    private CheckResult check(Object... args) throws Exception {
        final Stage stage = StageTestUtils.getNewStage();

        stage.setParameters(new KafkaOutputConfig(new Bag(args), null, templateUtils));

        return ((ActionPreChecker)preCheckerProvider.get()).preCheck(stage);
    }

}
