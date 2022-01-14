package io.datatok.djobi.plugins.stages.api.yarn.clean;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;

import java.util.ArrayList;

public class APIYarnCleanConfig extends ActionConfiguration {
    String url;
    ArrayList<String> whitelistApps;

    @SuppressWarnings("unchecked")
    APIYarnCleanConfig(Bag stageConfiguration, Job job, TemplateUtils templateUtils) {
        super(stageConfiguration, job, templateUtils);

        this.url = render("url");
        this.whitelistApps =  (ArrayList<String>) stageConfiguration.get("whitelist");
    }
}
