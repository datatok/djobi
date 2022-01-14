package io.datatok.djobi.plugins.apm_opentracing;

import io.datatok.djobi.application.plugins.Plugin;
import io.datatok.djobi.application.plugins.PluginBootstrap;
import io.datatok.djobi.engine.events.*;
import io.datatok.djobi.event.EventBus;
import io.datatok.djobi.plugins.apm_opentracing.subscribers.*;
import io.datatok.djobi.spark.executor.SparkExecutorEvent;
import io.datatok.djobi.utils.di.SimpleProviderFactory;

import javax.inject.Inject;

public class APMOpenTracingPlugin extends Plugin implements PluginBootstrap {

    @Inject
    private EventBus eventBus;

    @Inject
    private SimpleProviderFactory providerFactory;

    public Class<APMOpenTracingPlugin> getBootstrap() {
        return APMOpenTracingPlugin.class;
    }

    public void bootstrap() {
        eventBus.subscribe(JobRunStartEvent.NAME, providerFactory.get(JobLivecycleSubscriber.class));
        eventBus.subscribe(JobRunFinishEvent.NAME, providerFactory.get(JobLivecycleSubscriber.class));

        eventBus.subscribe(StageRunStartEvent.NAME, providerFactory.get(StageRunSubscriber.class));
        eventBus.subscribe(StageRunFinishEvent.NAME, providerFactory.get(StageRunSubscriber.class));

        eventBus.subscribe(StagePostCheckStartEvent.NAME, providerFactory.get(StagePostCheckEventsSubscriber.class));
        eventBus.subscribe(StagePostCheckDoneEvent.NAME, providerFactory.get(StagePostCheckEventsSubscriber.class));

        eventBus.subscribe(SparkExecutorEvent.NAME, providerFactory.get(SparkExecutorListenerSubscriber.class));

        eventBus.subscribe(PipelineRunStartEvent.NAME, providerFactory.get(PipelineRunSubscriber.class));
        eventBus.subscribe(PipelineRunFinishEvent.NAME, providerFactory.get(PipelineRunSubscriber.class));

        eventBus.subscribe(JobPhaseStartEvent.NAME, providerFactory.get(JobPhaseLivecycleSubscriber.class));
        eventBus.subscribe(JobPhaseFinishEvent.NAME, providerFactory.get(JobPhaseLivecycleSubscriber.class));

        eventBus.subscribe(ErrorEvent.NAME, providerFactory.get(EngineErrorSubscriber.class));
    }

}
