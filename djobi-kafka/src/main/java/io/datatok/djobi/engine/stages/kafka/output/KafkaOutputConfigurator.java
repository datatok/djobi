package io.datatok.djobi.engine.stages.kafka.output;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.engine.stage.livecycle.ActionSimpleConfigurator;

public class KafkaOutputConfigurator extends ActionSimpleConfigurator {
    @Override
    public ActionConfiguration configure(final Job job, Stage stage) {
        return new KafkaOutputConfig(stage.getSpec(), job, templateUtils);
    }
}
