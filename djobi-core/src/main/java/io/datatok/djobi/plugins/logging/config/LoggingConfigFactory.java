package io.datatok.djobi.plugins.logging.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.datatok.djobi.configuration.Configuration;

@Singleton
public class LoggingConfigFactory {

    final public static String CONFIG_PATH = "djobi.plugins.logger";

    final private static Config SINK_DEFAULTS = ConfigFactory.parseString("  ");

    @Inject
    private Configuration configuration;

    /**
     * Use main configuration.
     *
     * @return LoggingConfig
     */
    public LoggingConfig build() {
        return build(configuration.getConfig(CONFIG_PATH));
    }

    /**
     *
     * @param sourceConfig
     * @return LoggingConfig
     */
    static public LoggingConfig build(Config sourceConfig) {
        final Config sourceConfigWithDefaults = sourceConfig.withFallback(getDefaults());
        final LoggingConfig config = new LoggingConfig();

        config
            .setStageSink(buildSinkConfig(sourceConfigWithDefaults.getConfig("sinks.stages")))
            .setJobSink(buildSinkConfig(sourceConfigWithDefaults.getConfig("sinks.jobs")))
            .setMetricSink(buildSinkConfig(sourceConfigWithDefaults.getConfig("sinks.metrics")))
        ;

        return config;
    }

    static private LoggingSinkConfig buildSinkConfig(final Config raw) {
        final LoggingSinkConfig config = new LoggingSinkConfig();

        config
            .setEnabled(raw.getBoolean("enabled"))
            .setStoreType(raw.getString("type"))
            .setStoreURL(raw.getString("options.url"))
            .setStoreBucket(raw.getString("options.index"))
        ;

        return config;
    }

    static private Config getDefaults() {
        final String str = "sinks { " +
                "jobs { enabled = false, type = '', options { url = '', index = '' } } \n" +
                "stages { enabled = false, type = '', options { url = '', index = '' } } \n" +
                "metrics { enabled = false, type = '', options { url = '', index = '' } } \n" +
                "" +
                "}";

        return ConfigFactory.parseString(str);
    }

}
