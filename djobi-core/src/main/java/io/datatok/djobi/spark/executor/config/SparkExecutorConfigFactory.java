package io.datatok.djobi.spark.executor.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import io.datatok.djobi.configuration.Configuration;

import javax.inject.Inject;
import javax.inject.Singleton;

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

        return create(config);
    }

    public SparkExecutorConfig create(Config config) {
        return ConfigBeanFactory.create(config, SparkExecutorConfig.class);
    }

    private Config getDefaults() {
        final String buffer = """
            defaults {
                master: "local[1]"
                appName: "djobi-app"
            }
            yarnUrl: ""
            extraDataSources: []
            conf {}
        """;

        return ConfigFactory.parseString(buffer);
    }

}
