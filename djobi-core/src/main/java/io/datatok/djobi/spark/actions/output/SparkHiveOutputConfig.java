package io.datatok.djobi.spark.actions.output;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;

import java.util.List;

public class SparkHiveOutputConfig extends ActionConfiguration {
    String table;
    String path;
    int num_partitions;

    List<String> partitions;

    String checkQuery;

    public SparkHiveOutputConfig(Bag stageConfiguration, Job job, TemplateUtils templateUtils) {
        super(stageConfiguration, job, templateUtils);

        this.table = render("table");
        this.num_partitions = Integer.parseInt(render("num_partitions", "1"));
        this.partitions = renderList("partitions");
        this.path = render("path");
        this.checkQuery = render("check_query");
    }
}
