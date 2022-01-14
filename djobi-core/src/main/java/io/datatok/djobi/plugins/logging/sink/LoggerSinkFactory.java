package io.datatok.djobi.plugins.logging.sink;

import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.datatok.djobi.configuration.Configuration;
import io.datatok.djobi.plugins.logging.sink.elasticsearch.ElasticsearchLogSink;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LoggerSinkFactory {

    @Inject
    private Configuration configuration;

    @Inject
    private Injector injector;

    /**
     * Detect if sink is enabled or not.s
     *
     * @since v3.7.0
     * @param type String
     * @return boolean
     */
    public boolean enabled(final String type) {
        final String configPath = typeToConfigPath(type);

        return this.configuration.hasPath(configPath) && !this.configuration.getString(configPath + ".type").isEmpty();
    }

    public LogSink get(final String type) {
        final String configPath = typeToConfigPath(type);
        LogSink logSink = null;

        final Config sinkConfig = this.configuration.getConfig(configPath);

        final String sinkInstanceType = sinkConfig.getString("type");

        switch (sinkInstanceType) {
            case "elasticsearch":
                logSink = new ElasticsearchLogSink(sinkConfig.getConfig("options"));
            break;
            case "console":
                logSink = new ConsoleLogSink();
                break;
            default:
                logSink = new InMemoryLogSink();

        }

        injector.injectMembers(logSink);

        return logSink;

    }

    /**
     * Transform type into full configuration path.
     *
     * @since v3.7.0
     * @param type String
     * @return String
     */
    private String typeToConfigPath(final String type) {
        return "djobi.plugins.logger.sinks." + type;
    }

}
