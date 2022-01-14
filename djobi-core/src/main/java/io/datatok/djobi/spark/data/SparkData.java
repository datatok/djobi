package io.datatok.djobi.spark.data;

import io.datatok.djobi.engine.data.StageData;

public class SparkData<T> extends StageData<T> {

    public SparkData(String type, T data) {
        super(SparkDataKind.GROUP, type, data);
    }

}
