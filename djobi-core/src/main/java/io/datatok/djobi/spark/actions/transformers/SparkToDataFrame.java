package io.datatok.djobi.spark.actions.transformers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.data.BasicDataKind;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.engine.stage.livecycle.ActionSimpleConfigurator;
import io.datatok.djobi.exceptions.StageException;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.spark.executor.SparkExecutor;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.JSONUtils;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import java.util.List;
import java.util.stream.Collectors;

public class SparkToDataFrame {

    final static public String TYPE = "org.spark.transform.as_df";

    final static public String FORMAT_JSON = "json";
    final static public String FORMAT_JSON_ND = "json_nd";
    final static public String FORMAT_JSON_LINES = "json_lines";
    final static public String FORMAT_CSV = "csv";

    static final public class Configurator extends ActionSimpleConfigurator {
        @Override
        public ActionConfiguration configure(Job job, Stage stage) {
            return new Config(stage.getSpec(), job, templateUtils);
        }
    }

    static final public class Runner implements ActionRunner {
        @Override
        public ActionRunResult run(Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
            final Config config = (Config) stage.getParameters();
            final SparkExecutor executor = (SparkExecutor) contextExecutor;
            final JavaSparkContext javaSparkContext = executor.getJavaSparkContext();
            final SQLContext sqlContext = executor.getSQLContext();

            List<String> lines;

            switch (config.format) {
                case FORMAT_JSON:
                    lines = readContextDataFullJSON(contextData);
                break;
                case FORMAT_JSON_LINES:
                case FORMAT_JSON_ND:
                    lines = readContextData(contextData);
                 break;

                default:
                    throw new StageException(stage, "wrong data format!");
            }

            final JavaRDD<String> rdd = javaSparkContext.parallelize(lines);
            final Dataset<String> dfString = executor.getSession().createDataset(rdd.rdd(), Encoders.STRING());
            final Dataset<Row> df = sqlContext.read().json(dfString);

            if (config.as_table != null && !config.as_table.isEmpty()) {
                df.createOrReplaceTempView(config.as_table);
            }

            return ActionRunResult.success(new SparkDataframe(df));
        }
    }

    static final public class Config extends ActionConfiguration {

        public String as_table;
        public String format = FORMAT_JSON_ND;

        public Config(Bag stageConfiguration, Job job, TemplateUtils templateUtils) {
            super(stageConfiguration, job, templateUtils);

            if (this.has("as_table")) {
                this.as_table = render("as_table");
            }

            if (this.has("format")) {
                this.format = render("format");
            }
        }
    }

    static private List<String> readContextDataFullJSON(StageData<?> contextData) throws Exception
    {
        if (contextData.getKind().getType().equals(BasicDataKind.TYPE_STRING))
        {
            final String data = (String) contextData.getData();
            final List<?> lines = JSONUtils.parse(data, List.class);

            return lines
                        .stream().map(item -> {
                            try {
                                return JSONUtils.serialize(item);
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                            return "";
                        })
                        .collect(Collectors.toList());
        }

        throw new Exception("context data must be a string!");
    }

    static private List<String> readContextData(StageData<?> contextData) throws Exception
    {
        if (contextData.getKind().getType().equals(BasicDataKind.TYPE_LIST)) {

            List<?> lines = (List<?>) contextData.getData();

            if (! (lines.get(0) instanceof String))
            {
                throw new Exception("list item must be string!");
            }

            return lines.stream().map(s -> (String) s).collect(Collectors.toList());
        }

        throw new Exception("context data must be a list of strings!");
    }

}
