package io.datatok.djobi.spark.actions.collectors;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.data.BasicDataKind;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.data.SparkDataframe;
import org.apache.spark.sql.Row;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Transform a Spark DataFrame into a grouped by week object.
 * <!> Collect the DataFrame via the driver </!>
 */
public class ByWeekTransformer implements ActionRunner {

    static public final String TYPE = "transform.by_week";

    public ActionRunResult run(final Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception
    {
            final Job Job = stage.getJob();

            if (contextData instanceof SparkDataframe) {
                final List<Row> rows = ((SparkDataframe) contextData).getData().collectAsList();
                final HashMap<String, Object> results = new HashMap<>();

                final Object[] items = rows
                        .stream()
                        .map(r -> r.getAs("week"))
                        .distinct()
                        //.sorted(Comparator.comparing(w -> ((String) w)).reversed())
                        .map(w ->
                        {
                            final HashMap<String, Object> ret = new HashMap<>();
                            final Object[] days = rows
                                    .stream()
                                    .filter(r -> r.getAs("week").equals(w))
                                    .map(r ->
                                    {
                                        final HashMap<String, Object> ret2 = new HashMap<>();

                                        Arrays.stream(r.schema().fieldNames()).forEach(f ->
                                        {
                                            ret2.put(f, r.getAs(f));
                                        });

                                        return ret2;
                                    })
                                    .sorted(Comparator.comparing(d -> ((String) d.get("day"))))
                                    .toArray();

                            ret.put("week", w);
                            ret.put("days", days);
                            ret.put("days_length", days.length);

                            return ret;
                        })
                        .toArray();

                results.put("items", items);

                if (items.length > 0) {
                    results.put("week", ((HashMap<String, Object>) items[0]).get("week"));
                } else {
                    results.put("week", 0);
                }

                return ActionRunResult.success(new StageData<HashMap<String, Object>>(BasicDataKind.GROUP, BasicDataKind.TYPE_MAP, results));
            }

            throw new Exception("data must be DataFrame!");
        }
}
