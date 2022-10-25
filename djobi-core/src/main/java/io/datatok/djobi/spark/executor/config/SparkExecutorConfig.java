package io.datatok.djobi.spark.executor.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import io.datatok.djobi.configuration.Configuration;
import io.datatok.djobi.utils.MyMapUtils;

import java.util.List;
import java.util.Map;

public class SparkExecutorConfig {

    private String master;

    private String appName;

    private String yarnUrl;

    /**
     * Hold extra data sources
     */
    private List<SparkExecutorDataSourceConfig> extraDataSources;

    /**
     * eg: http://XXXX/history/{{app_id}}/jobs/job/?id={{id}}
     */
    private String webHistoryUrlForJob;

    /**
     * Hold spark configuration
     */
    private Map<String, Object> config;

    public SparkExecutorConfig() {
    }

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

    public String getYarnUrl() {
        return yarnUrl;
    }

    public void setYarnUrl(String yarnUrl) {
        this.yarnUrl = yarnUrl;
    }

    public String getWebHistoryUrlForJob() {
        return webHistoryUrlForJob;
    }

    public void setWebHistoryUrlForJob(String webHistoryUrlForJob) {
        this.webHistoryUrlForJob = webHistoryUrlForJob;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    public List<SparkExecutorDataSourceConfig> getExtraDataSources() {
        return extraDataSources;
    }

    public void setExtraDataSources(List<SparkExecutorDataSourceConfig> extraDataSources) {
        this.extraDataSources = extraDataSources;
    }
}
