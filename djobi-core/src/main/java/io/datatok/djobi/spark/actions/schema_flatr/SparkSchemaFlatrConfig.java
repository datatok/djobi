package io.datatok.djobi.spark.actions.schema_flatr;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;

import java.util.List;

public class SparkSchemaFlatrConfig extends ActionConfiguration {

    List<String> excludes;

    public SparkSchemaFlatrConfig(Bag stageConfiguration, Job job, TemplateUtils templateUtils) {
        super(stageConfiguration, job, templateUtils);

        this.excludes = renderList("excludes");
    }
}
