package io.datatok.djobi.spark.executor.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import io.datatok.djobi.configuration.Configuration;
import io.datatok.djobi.utils.MyMapUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Create Spark executor config bean.
 * From "djobi.executors.spark" key.
 */
@Singleton
public class SparkExecutorConfigFactory {

    @Inject
    Configuration configuration;

    public SparkExecutorConfig create() {
        Config config = configuration.getConfig("djobi.executors.spark").withFallback(
            getDefaults()
        );

        return ConfigBeanFactory.create(config, SparkExecutorConfig.class);
    }

    private Config getDefaults() {
        final String buffer = """
            master: "localhost[1]"
            appName: "djobi-app"
            yarnUrl: ""
            extraDataSources: {}
            config {}
        """;

        return ConfigFactory.parseString(buffer);
    }

}
