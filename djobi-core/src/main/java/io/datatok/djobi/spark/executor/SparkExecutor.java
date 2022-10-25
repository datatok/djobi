package io.datatok.djobi.spark.executor;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.executor.config.SparkExecutorConfig;
import io.datatok.djobi.spark.executor.config.SparkExecutorConfigFactory;
import io.datatok.djobi.spark.executor.config.SparkExecutorDataSourceConfig;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.MyMapUtils;
import io.datatok.djobi.utils.http.Http;
import org.apache.hadoop.fs.FileSystem;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class SparkExecutor implements Executor {

    final static public String TYPE = "spark";

    private static final Logger logger = Logger.getLogger(SparkExecutor.class);

    private final SparkExecutorConfig configuration;

    @Inject
    private Http http;

    @Inject
    private SparkReporter reporter;

    protected SparkSession sparkSession;
    protected SQLContext sqlContext;
    protected SparkContext sparkContext;
    protected JavaSparkContext javaSparkContext;
    protected FileSystem hdfs;

    protected Bag lastConf;

    protected boolean connected = false;

    @Inject
    public SparkExecutor(SparkExecutorConfigFactory configFactory) {
        this.configuration = configFactory.create();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void connect() throws IOException {

        if (connected) {
            return ;
        }

        final SparkConf esSparkConf = new SparkConf();

        sparkConfigure(esSparkConf);

        if (SparkSession.getActiveSession().nonEmpty()) {
            this.sparkSession = SparkSession.getActiveSession().get();
        } else {
            this.sparkSession = SparkSession.builder()
                    .config(esSparkConf)
                    .getOrCreate();
        }

        this.sqlContext = sparkSession.sqlContext();
        this.sparkContext = sparkSession.sparkContext();
        this.javaSparkContext = new JavaSparkContext(sparkContext);

        this.reporter.setCurrentApplicationID(this.sparkContext.applicationId());

        sparkContext.addSparkListener(this.reporter);

        this.hdfs = FileSystem.get(sparkSession.sparkContext().hadoopConfiguration());

        connected = true;

        setupData();
    }

    @Override
    public void configure(Bag conf) {
        this.lastConf = conf;
    }

    @Override
    public void setCurrentStage(Stage stage) {
        this.reporter.setCurrentStage(stage);

        if (this.sparkContext != null) {
            this.sparkContext.setJobGroup(
                String.format("%s-%s-%s", stage.getJob().getWorkflow().getName(), stage.getJob().getId(), stage.getName()),
                String.format("%s-%s-%s", stage.getJob().getWorkflow().getName(), stage.getJob().getName(), stage.getName()),
true
            );
        }
    }

    public Map<String, String> getMeta() throws IOException {
        if (!connected) {
            connect();
        }

        return MyMapUtils.map(
                "spark", MyMapUtils.map(
                    "version", sparkContext.version(),
                    "application", MyMapUtils.map(
                        "name", sparkContext.appName(),
                        "id", sparkContext.applicationId()
                    ),
                    "executor", MyMapUtils.map(
                        "memory", sparkContext.executorMemory()
                    ),
                    "master", sparkContext.master()
            ),
            "user", sparkContext.sparkUser()
        );
    }

    @Override
    public Object get(String service) throws Exception {
        if (!connected) {
            connect();
        }

        switch (service) {
            case "hdfs":
            case "fs":
                return this.hdfs;
            case "sql_context":
                return this.sqlContext;
            case "context":
                return this.sparkContext;
            case "java_context":
                return this.javaSparkContext;
        }

        throw new Exception("Service not found!");
    }

    @Override
    public String getTitle() {
        return this.sparkContext.appName();
    }

    @Override
    public Map<String, String> getLogs() {
        final HashMap<String, String> files = new HashMap<>();
        final String yarnBaseUrl = configuration.getYarnUrl();

        if (yarnBaseUrl != null && yarnBaseUrl.length() > 0) {
            final String yarnUrl = String.format(yarnBaseUrl + "/ws/v1/cluster/apps/%s/appattempts", sparkContext.applicationId());

            try {
                final String logURl = (String) http.get(yarnUrl).execute().at("appAttempts.appAttempt.0.logsLink");

                if (logURl != null) {
                    files.put("index", http.get(logURl).execute().raw());
                    files.put("stderr", http.get(logURl + "/stderr/?start=0").execute().raw());
                    files.put("stdout", http.get(logURl + "/stdout/?start=0").execute().raw());
                    files.put("launch_container.sh", http.get(logURl + "/launch_container.sh/?start=0").execute().raw());
                } else {
                    logger.error(String.format("Cannot retrieve log URL at %s!", yarnUrl));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return files;
    }

    protected void sparkConfigure(final SparkConf conf) {

        if (configuration.getMaster() != null && !configuration.getMaster().isEmpty()) {
            conf.setMaster(configuration.getMaster());
        }

        conf.setAppName(configuration.getAppName());

        for (Map.Entry<String, String> entry : MyMapUtils.<String>flattenKeys(configuration.getConfig()).entrySet()) {
            logger.debug(String.format("configure Spark executor %s = %s", entry.getKey(), entry.getValue()));
            conf.set(entry.getKey(), entry.getValue());
        }

        if (this.lastConf != null && this.lastConf.containsKey("conf")) {
            final Map<String, Object> sparkConf = (Map<String, Object>) this.lastConf.get("conf");

            for (Map.Entry<String, Object> entry : sparkConf.entrySet()) {
                logger.debug(String.format("configure Spark executor %s = %s", entry.getKey(), entry.getValue().toString()));
                conf.set(entry.getKey(), entry.getValue().toString());
            }
        }
    }


    public Map<String, Object> toHash() {
        return MyMapUtils.map(
                "type", TYPE
        );
    }

    public SparkContext getSparkContext() throws IOException {
        if (!connected) {
            connect();
        }

        return sparkContext;
    }

    public JavaSparkContext getJavaSparkContext() throws IOException {
        if (!connected) {
            connect();
        }

        return this.javaSparkContext;
    }

    public SQLContext getSQLContext() throws IOException {
        if (!isConnected())
        {
            connect();
        }

        return sqlContext;
    }

    public FileSystem getHDFS() {
        return hdfs;
    }

    public SparkSession getSession() { return sparkSession;}

    public boolean isConnected() {
        return connected;
    }

    private void setupData() {
        for (SparkExecutorDataSourceConfig dsConfig : configuration.getExtraDataSources()) {
            logger.info(String.format("Setup table %s", dsConfig.getName()));

            switch (dsConfig.getType()) {
                case "table" -> {
                    Dataset<Row> df = sqlContext
                        .read()
                        .format(dsConfig.getFormat())
                        .load(dsConfig.getPath());
                    /*
                    if (dataItem.hasPath("columns")) {
                        for (Map.Entry<String, ConfigValue> column : dataItem.getObject("columns").entrySet()) {
                            df = df.withColumn(column.getKey(), functions.lit(column.getValue().render()));
                        }
                    }*/
                    df.createOrReplaceTempView(dsConfig.getName());
                }
                case "sql" -> sqlContext
                    .sql(dsConfig.getSql());
            }
        }
    }


    static private void copyConf(Config config, SparkConf target, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (config.hasPath(entry.getKey())) {
                target.set("spark." + entry.getValue(), config.getString(entry.getKey()));
            }
        }
    }
}
