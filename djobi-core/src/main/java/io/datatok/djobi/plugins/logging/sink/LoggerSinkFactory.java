package io.datatok.djobi.plugins.logging.sink;

import com.google.inject.Injector;
import io.datatok.djobi.plugins.logging.config.*;
import io.datatok.djobi.plugins.logging.sink.elasticsearch.ElasticsearchLogSink;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LoggerSinkFactory {

    @Inject
    private LoggingConfigFactory configFactory;

    @Inject
    private Injector injector;

    public LogSink get(final String type) {
        final LoggingConfig config = configFactory.build();
        final LoggingSinkConfig sinkConfig = config.getSinkByType(type);
        LogSink logSink = null;

        switch (sinkConfig.getStoreType()) {
            case "elasticsearch":
                logSink = new ElasticsearchLogSink(sinkConfig);
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
}
