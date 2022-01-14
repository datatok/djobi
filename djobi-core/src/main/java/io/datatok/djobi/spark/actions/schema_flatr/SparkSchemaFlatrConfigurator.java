package io.datatok.djobi.spark.actions.schema_flatr;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.engine.stage.livecycle.ActionSimpleConfigurator;

public class SparkSchemaFlatrConfigurator extends ActionSimpleConfigurator {
    @Override
    public ActionConfiguration configure(final Job job, Stage stage) {
        return new SparkSchemaFlatrConfig(stage.getSpec(), job, templateUtils);
    }
}
