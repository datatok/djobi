package io.datatok.djobi.spark.actions;

import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.spark.actions.mutate.SparkMutateType;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.spark.executor.SparkExecutor;
import io.datatok.djobi.test.ActionTest;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.MyMapUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SparkMutateRunnerTest extends ActionTest {

    @Test
    public void testCast() throws Exception {

        final SparkExecutor executor = getSparkExecutor();

        executor.connect();

        Dataset<Row> df = buildDataset(3);

        df = df
            .withColumn ("dec", df.col("d").cast(DataTypes.createDecimalType(4,2)))
            .withColumn ("dec2", df.col("d").cast(DataTypes.createDecimalType(4,2)))
        ;

        //df.printSchema();
        //df.show();

        ActionRunResult runResult = stageTestUtils.builder()
            .withData(new SparkDataframe(df))
            .attachExecutor(executor)
            .addStageType(SparkMutateType.TYPE)
            .configure(new Bag("cast", MyMapUtils.mapString("d", "float", "dec", "float")))
            .run();

        final Dataset<Row> newDF = ((SparkDataframe) runResult.getData()).getData();

        StructType schema = newDF.schema();
        StructField[] fields = schema.fields();

        Assertions.assertEquals("float", fields[schema.fieldIndex("d")].dataType().typeName());
        Assertions.assertEquals("double", fields[schema.fieldIndex("d2")].dataType().typeName());
        Assertions.assertEquals("float", fields[schema.fieldIndex("dec")].dataType().typeName());
        Assertions.assertEquals(DataTypes.createDecimalType(4,2), fields[schema.fieldIndex("dec2")].dataType());
        Assertions.assertEquals(((Double) df.collectAsList().get(0).get(0)).floatValue(), newDF.collectAsList().get(0).get(0));

        //newDF.printSchema();
        //newDF.show();
    }

    @Test
    public void testSort() throws Exception {
        final SparkExecutor executor = getSparkExecutor();

        executor.connect();

        Dataset<Row> df = buildDataset(10);

        ActionRunResult runResult = stageTestUtils.builder()
                .withData(new SparkDataframe(df))
                .attachExecutor(executor)
                .addStageType(SparkMutateType.TYPE)
                .configure(new Bag("sort_column", "i", "sort_direction", "desc"))
                .run();

        final Dataset<Row> newDF = ((SparkDataframe) runResult.getData()).getData();
        final List<Row> rows = newDF.collectAsList();

        StructType schema = newDF.schema();

        for (int i = 0; i < 9; i++) {
            int a = rows.get(i).getInt(schema.fieldIndex("i"));
            int b = rows.get(i + 1).getInt(schema.fieldIndex("i"));

            Assertions.assertTrue(a >= b, String.format("%d >= %d", a, b));
        }
    }

}
