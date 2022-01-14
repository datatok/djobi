package io.datatok.djobi.engine.stages.elasticsearch.input;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.engine.stage.livecycle.ActionSimpleConfigurator;

public class ESSparkInputConfigurator extends ActionSimpleConfigurator {
    @Override
    public ActionConfiguration configure(final Job job, Stage stage) {
        return new ESSparkInputConfig(stage.getSpec(), job, templateUtils);
    }
}
