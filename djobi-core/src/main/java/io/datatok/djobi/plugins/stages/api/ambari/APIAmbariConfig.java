package io.datatok.djobi.plugins.stages.api.ambari;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;

public class APIAmbariConfig extends ActionConfiguration {
    String action;
    String service;
    String component;
    String url;
    String cluster;
    String credentials;

    public APIAmbariConfig(Bag stageConfiguration, Job job, TemplateUtils templateUtils) {
        super(stageConfiguration, job, templateUtils);

        this.action = render("action");
        this.cluster = render("cluster");
        this.url = render("url");
        this.component = render("component");
        this.service = render("service");
        this.credentials = render("credentials");
    }
}
