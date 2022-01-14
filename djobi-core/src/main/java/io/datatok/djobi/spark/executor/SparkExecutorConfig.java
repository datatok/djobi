package io.datatok.djobi.spark.executor;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.datatok.djobi.configuration.Configuration;
import io.datatok.djobi.utils.MyMapUtils;

import java.util.Map;

public class SparkExecutorConfig {

    private Config data;

    private Config raw;

    private String yarnUrl;

    /**
     * eg: http://XXXX/history/{{app_id}}/jobs/job/?id={{id}}
     */
    private String webHistoryUrlForJob;

    @Inject
    public SparkExecutorConfig(Configuration configuration) {
        this.raw = configuration.getConfig("djobi.executors.spark").withFallback(ConfigFactory.parseMap(
            getDefaults()
        ));

        this.data = this.raw.getConfig("data");
        this.yarnUrl = this.raw.getString("logger.yarn");
        this.webHistoryUrlForJob = this.raw.getString("webHistoryUrlForJob");
    }

    public Config getData() {
        return data;
    }

    public Config getRaw() {
        return raw;
    }

    public String getYarnUrl() {
        return yarnUrl;
    }

    public String getWebHistoryUrlForJob() {
        return webHistoryUrlForJob;
    }

    private Map<String, ? extends Object> getDefaults() {
        return MyMapUtils.map("data", MyMapUtils.map(), "logger", MyMapUtils.map( "yarn", ""));
    }
}
