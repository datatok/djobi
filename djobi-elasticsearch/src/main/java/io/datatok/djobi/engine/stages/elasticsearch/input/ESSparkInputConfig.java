package io.datatok.djobi.engine.stages.elasticsearch.input;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;

import java.util.Map;

public class ESSparkInputConfig extends ActionConfiguration {

    String url;
    String index;
    String query;
    String data_type;
    String table;
    Map<String, String> esOptions;

    ESSparkInputConfig(Bag stageConfiguration, Job job, TemplateUtils templateUtils) {
        super(stageConfiguration, job, templateUtils);

        this.url = render("url");
        this.index = render("index");
        this.query = render("query");
        this.data_type = render("data_type", "dataframe");
        this.table = render("table");
        this.esOptions = renderMap("elasticsearch", true);
    }

}
