package io.datatok.djobi.engine.stage.livecycle;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.utils.templating.TemplateUtils;

import javax.inject.Inject;

abstract public class ActionSimpleConfigurator implements ActionConfigurator {

    @Inject
    protected TemplateUtils templateUtils;

    abstract public ActionConfiguration configure(final Job job, Stage stage);
}
