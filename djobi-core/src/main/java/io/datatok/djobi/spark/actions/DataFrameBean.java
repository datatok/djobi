package io.datatok.djobi.spark.actions;

import java.io.Serializable;

public interface DataFrameBean extends Serializable {

    void transform(Object rddData);

}
