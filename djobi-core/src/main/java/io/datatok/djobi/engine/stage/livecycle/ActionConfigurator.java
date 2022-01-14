package io.datatok.djobi.engine.stage.livecycle;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.Stage;

public interface ActionConfigurator extends Action {
    ActionConfiguration configure(final Job job, Stage stage);
}
