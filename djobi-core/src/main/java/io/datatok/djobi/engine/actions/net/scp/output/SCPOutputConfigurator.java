package io.datatok.djobi.engine.actions.net.scp.output;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.engine.stage.livecycle.ActionSimpleConfigurator;

public class SCPOutputConfigurator extends ActionSimpleConfigurator {
    @Override
    public ActionConfiguration configure(final Job job, Stage stage) {
        return new SCPOutputConfig(stage.getSpec(), job, templateUtils);
    }
}
