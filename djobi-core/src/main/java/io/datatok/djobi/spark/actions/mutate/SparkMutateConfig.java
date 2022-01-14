package io.datatok.djobi.spark.actions.mutate;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;

import java.util.List;
import java.util.Map;

public class SparkMutateConfig extends ActionConfiguration {
    // Rename fields
    Map<String, String> renames;
    // Delete fields
    List<String> deletes;
    // Register temp table
    String alias_table;
    // Transform RDD => DataFrame
    String dataframe_class;

    // Order by
    String sortColumn;
    String sortDirection;

    // Limit DataFrame
    int limit;
    // Adds
    Map<String, String> adds;

    // Cast
    Map<String, String> cast;

    int repartition_count;
    String repartition_column;

    boolean broadcast;

    SparkMutateConfig(Bag stageConfiguration, Job job, TemplateUtils templateUtils) {
        super(stageConfiguration, job, templateUtils);

        this.renames = renderMap("renames", true);
        this.adds = renderMap("adds", true);
        this.cast = renderMap("cast", true);
        this.deletes = renderList("deletes");
        this.alias_table = render("alias_table");
        this.dataframe_class = render("dataframe_class");
        this.limit = Integer.parseInt(render("limit", "0"));
        this.sortColumn = render("sort_column");
        this.sortDirection = render("sort_direction");

        this.repartition_column = render("repartition_column");
        this.repartition_count = Integer.parseInt(render("repartition_count", "0"));

        this.broadcast = Boolean.parseBoolean(render("broadcast", "false"));
    }
}
