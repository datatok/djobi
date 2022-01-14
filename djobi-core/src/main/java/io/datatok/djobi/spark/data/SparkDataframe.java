package io.datatok.djobi.spark.data;

import io.datatok.djobi.engine.data.StageData;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

/**
 * Hold data-frame (aka Dataset<Row>) data.
 */
public class SparkDataframe extends StageData<Dataset<Row>> {

    public SparkDataframe(Dataset<Row> data) {
        super(SparkDataKind.GROUP, SparkDataKind.TYPE_DATASET, SparkDataKind.GROUP, SparkDataKind.TYPE_ROW, data);
    }

}
