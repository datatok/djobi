package io.datatok.djobi.engine.actions.utils.generator;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;

public class RandomGeneratorConfig extends ActionConfiguration {
    public Integer count;

    @SuppressWarnings("unchecked")
    RandomGeneratorConfig(Bag stageConfiguration, Job job, TemplateUtils templateUtils) {
        super(stageConfiguration, job, templateUtils);

        this.count = Integer.parseInt(render("count"));
    }
}
