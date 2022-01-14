package io.datatok.djobi.spark.actions.collectors;

import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.data.StageDataList;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.exceptions.StageException;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.actions.SparkActionProvider;
import io.datatok.djobi.spark.data.SparkDataKind;
import io.datatok.djobi.spark.data.SparkDataframe;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This transform a Spark DataFrame into a HashMap<String, Object>
 *  <!> Collect the data-frame via the driver </!>
 */
public class CollectRunner implements ActionRunner {

    static public final String TYPE = SparkActionProvider.resolveFullname("collect");

    static private final Logger logger = Logger.getLogger(CollectRunner.class);

    public ActionRunResult run(final Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        if (!SparkDataKind.isSparkData(contextData))
        {
            throw new Exception("data must be from Spark!");
        }

        Object inData = contextData.getData();

        if (contextData instanceof SparkDataframe || inData instanceof Dataset)
        {
            final List<?> rows = ((Dataset<?>) contextData.getData()).collectAsList();

            logger.info("Collect " + rows.size() + " rows!");

            final Object firstObject = rows.get(0);
            List<Object> outData;

            if (firstObject instanceof String)
            {
                outData = rows
                            .stream()
                            .map(c -> (String) c)
                            .collect(Collectors.toList());
            }
            else if (firstObject instanceof Row)
            {
                outData = rows
                            .stream()
                            .map(r -> (Row) r)
                            .map(CollectRunner::convertRowToMap)
                            .collect(Collectors.toList());
            }
            else
            {
                throw new StageException(stage, "item type must be [Row, String]");
            }

            final StageDataList<Object> stageData = new StageDataList<>(outData);

            return ActionRunResult.success(stageData);
        }
        else if (inData instanceof JavaRDD)
        {
            final List<?> rows = ((JavaRDD<?>) inData).collect();
            final List<String> rowsAsString = new ArrayList<>();

            for(Object buffer : rows)
            {
                rowsAsString.add((String) buffer);
            }

            logger.info("Collect " + rows.size() + " rows!");

            final StageDataList<String> stageData = new StageDataList<>(rowsAsString);

            return ActionRunResult.success(stageData);
        } else {
            throw new Exception("data must be DataFrame or RDD!");
        }
    }

    static private HashMap<String, Object> convertRowToMap(final Row r) {
        final HashMap<String, Object> ret2 = new HashMap<>();

        Arrays.stream(r.schema().fieldNames()).forEach(f ->
        {
            Object obj = r.getAs(f);

            if (obj instanceof Row) {
                ret2.put(f, convertRowToMap((Row) obj));
            } else {
                ret2.put(f, obj);
            }
        });

        return ret2;
    }
}
