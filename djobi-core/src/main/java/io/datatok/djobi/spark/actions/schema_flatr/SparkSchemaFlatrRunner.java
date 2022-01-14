package io.datatok.djobi.spark.actions.schema_flatr;

import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.data.SparkDataframe;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;

import java.util.Arrays;
import java.util.stream.Stream;

public class SparkSchemaFlatrRunner implements ActionRunner {
    @Override
    public ActionRunResult run(final Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {

        Dataset<Row> df = ((SparkDataframe) contextData).getData();
        Column[] columns = flattenStructSchema(df.schema(), "").toArray(Column[]::new);
        Dataset<Row> newDF = df.select(columns);

        return ActionRunResult.success(new SparkDataframe(newDF));
    }

    static public Stream<Column> flattenStructSchema(StructType schema, String prefix)
    {
        return Arrays.stream(schema.fields()).flatMap(f -> {
            final String columnName = prefix.isEmpty() ? f.name() : (prefix + "." + f.name());

            if (f.dataType().typeName().equals("struct"))
            {
                return flattenStructSchema((StructType) f.dataType(), columnName);
            }

            return Stream.of(new Column(columnName).as(columnName.replace(".", "_")));
        });
    }
}
