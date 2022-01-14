package io.datatok.djobi.engine.actions.sql;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;

public class SQLConfig extends ActionConfiguration {

    public String check_path;
    public String query;

    SQLConfig(Bag stageConfiguration, Job job, TemplateUtils templateUtils) {
        super(stageConfiguration, job, templateUtils);

        this.check_path = render("check_path", "");
        this.query = render("query");
    }

}
