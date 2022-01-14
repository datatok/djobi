package io.datatok.djobi.spark.data;

import io.datatok.djobi.engine.data.StageData;

public class SparkDataKind {

    static final public String GROUP = "spark";

    static final public String TYPE_DATASET = "dataset";
    static final public String TYPE_ROW = "row";
    static final public String TYPE_RDD = "rdd";
    static final public String TYPE_JAVA_RDD = "java_rdd";

    /**
     * Check if stage data is a Spark format.
     *
     * @param stageData
     * @return
     */
    static public boolean isSparkData(StageData<?> stageData)
    {
        return stageData.getKind().getProvider().equals(GROUP);
    }

}
