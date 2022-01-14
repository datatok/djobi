package io.datatok.djobi.engine.actions.csv;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.engine.stage.livecycle.ActionSimpleConfigurator;

public class CSVFilterConfigurator extends ActionSimpleConfigurator {
    @Override
    public ActionConfiguration configure(final Job job, Stage stage) {
        return new CSVFilterConfig(stage.getSpec(), job, templateUtils);
    }
}
