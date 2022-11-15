package io.datatok.djobi.engine.actions.fs.input;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;

public class FSInputConfig extends ActionConfiguration {

    public String format;
    public String path;
    public String paths;
    

    FSInputConfig(final Bag stageConfiguration, final Job job, final TemplateUtils templateUtils) {
        super(stageConfiguration, job, templateUtils);

        this.format = render("format", "json");
        this.path = renderPath("path", "");
        this.paths = render("paths", "");
    }
}
