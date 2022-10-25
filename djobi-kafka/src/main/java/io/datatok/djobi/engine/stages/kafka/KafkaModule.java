package io.datatok.djobi.engine.stages.kafka;

import io.datatok.djobi.engine.flow.ExecutionContext;
import io.datatok.djobi.engine.stage.ActionProvider;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stages.kafka.output.KafkaOutputConfigurator;
import io.datatok.djobi.engine.stages.kafka.output.KafkaOutputRunner;
import io.datatok.djobi.engine.stages.kafka.output.KafkaOutputType;
import io.datatok.djobi.engine.stages.kafka.output.KafkaPreChecker;
import io.datatok.djobi.engine.utils.StageCandidate;

public class KafkaModule extends ActionProvider {

    static public final String NAME = "org.apache.kafka";

    @Override
    public void configure() {
        registerConfigurator(KafkaOutputType.TYPE, KafkaOutputConfigurator.class);
        registerRunner(KafkaOutputType.TYPE, KafkaOutputRunner.class);
        registerPreChecker(KafkaOutputType.TYPE, KafkaPreChecker.class);
    }

    @Override
    public StageCandidate getGenericActionCandidates(Stage stage, String phase, ExecutionContext executionContext) {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
