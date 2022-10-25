package io.datatok.djobi.spark.executor.config;

import com.typesafe.config.Optional;
import io.datatok.djobi.utils.MyMapUtils;

import java.util.List;
import java.util.Map;

public class SparkExecutorConfig {

    @Optional
    private String master;

    @Optional
    private String appName;

    @Optional
    private String yarnUrl;

    /**
     * Hold extra data sources
     */
    @Optional
    private List<SparkExecutorDataSourceConfig> extraDataSources;

    /**
     * eg: http://XXXX/history/{{app_id}}/jobs/job/?id={{id}}
     */
    @Optional
    private String webHistoryUrlForJob;

    /**
     * Hold spark configuration
     */
    @Optional
    private Map<String, Object> conf;

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

    public Map<String, Object> getConf() {
        return conf;
    }

    public Map<String, String> getConfFlatten() {
        if (getConf() == null) {
            return null;
        }

        return MyMapUtils.valuesToString(MyMapUtils.flattenKeys(getConf()));
    }

    public void setConf(Map<String, Object> conf) {
        this.conf = conf;
    }

    public List<SparkExecutorDataSourceConfig> getExtraDataSources() {
        return extraDataSources;
    }

    public void setExtraDataSources(List<SparkExecutorDataSourceConfig> extraDataSources) {
        this.extraDataSources = extraDataSources;
    }
}
