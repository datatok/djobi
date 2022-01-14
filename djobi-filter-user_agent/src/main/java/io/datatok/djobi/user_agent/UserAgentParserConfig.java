package io.datatok.djobi.user_agent;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;

import java.util.List;

public class UserAgentParserConfig extends ActionConfiguration {

    String fieldSource;
    String fieldTarget;

    List<String> fields;

    public UserAgentParserConfig(Bag stageConfiguration, Job job, TemplateUtils templateUtils) {
        super(stageConfiguration, job, templateUtils);

        this.fieldSource = render("source");
        this.fieldTarget = render("target");
        this.fields = renderList("fields");
    }
}
