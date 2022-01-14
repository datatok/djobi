package io.datatok.djobi.engine.actions.transformer.serializer;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;

public class SerializerConfig extends ActionConfiguration {

    String format;
    Boolean collect;

    SerializerConfig(final Bag stageConfiguration, final Job job, final TemplateUtils templateUtils) {

        super(stageConfiguration, job, templateUtils);

        this.format = render("format", "json");
        this.collect = Boolean.parseBoolean(render("collect", "false"));
    }
}