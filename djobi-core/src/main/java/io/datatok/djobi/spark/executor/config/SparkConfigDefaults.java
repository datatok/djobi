package io.datatok.djobi.spark.executor.config;

import com.typesafe.config.Optional;

public class SparkConfigDefaults {
    @Optional
    private String master;

    @Optional
    private String appName;

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
