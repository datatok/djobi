package io.datatok.djobi.plugins.logging;

import io.datatok.djobi.application.plugins.Plugin;
import io.datatok.djobi.application.plugins.PluginBootstrap;
import io.datatok.djobi.engine.events.*;
import io.datatok.djobi.event.EventBus;
import io.datatok.djobi.executors.events.MetricAvailableEvent;
import io.datatok.djobi.plugins.logging.config.LoggingConfig;
import io.datatok.djobi.plugins.logging.config.LoggingConfigFactory;
import io.datatok.djobi.plugins.logging.subcribers.*;
import io.datatok.djobi.utils.di.SimpleProviderFactory;

import javax.inject.Inject;

public class LoggingPlugin extends Plugin implements PluginBootstrap {

    @Inject
    private EventBus eventBus;

    @Inject
    private SimpleProviderFactory providerFactory;

    @Inject
    private LoggingConfigFactory configFactory;

    public Class<LoggingPlugin> getBootstrap() {
        return LoggingPlugin.class;
    }

    public void bootstrap() {
        final LoggingConfig config = configFactory.build();

        if (config.getJobSink().isEnabled()) {
            eventBus.subscribe(JobRunStartEvent.NAME, providerFactory.get(JobRunStartSubscriber.class));
            eventBus.subscribe(JobRunFinishEvent.NAME, providerFactory.get(JobRunFinishSubscriber.class));
        }

        if (config.getMetricSink().isEnabled()) {
            eventBus.subscribe(MetricAvailableEvent.NAME, providerFactory.get(MetricsLogger.class));
        }

        if (config.getStageSink().isEnabled()) {
            eventBus.subscribe(StageRunStartEvent.NAME, providerFactory.get(StageRunSubscriber.class));
            eventBus.subscribe(StageRunFinishEvent.NAME, providerFactory.get(StageRunSubscriber.class));
            eventBus.subscribe(StagePreCheckDoneEvent.NAME, providerFactory.get(StagePostCheckSubscriber.class));
            eventBus.subscribe(StagePostCheckDoneEvent.NAME, providerFactory.get(StagePostCheckSubscriber.class));
        }

        /*if (sinkFactory.enabled(LoggerTypes.TYPE_LOGS)) {
            providerFactory.get(Log4jLogger.class).get().call(null);
        }*/
    }

}
