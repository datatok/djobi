package io.datatok.djobi.plugins.stages.api.ambari;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.engine.stage.livecycle.ActionSimpleConfigurator;

public class APIAmbariConfigurator extends ActionSimpleConfigurator {
    @Override
    public ActionConfiguration configure(final Job job, Stage stage) {
        return new APIAmbariConfig(stage.getSpec(), job, templateUtils);
    }
}
