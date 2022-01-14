package io.datatok.djobi.plugins.report;

import io.datatok.djobi.application.plugins.Plugin;
import io.datatok.djobi.application.plugins.PluginBootstrap;
import io.datatok.djobi.engine.events.JobRunFinishEvent;
import io.datatok.djobi.engine.events.JobRunStartEvent;
import io.datatok.djobi.engine.events.PipelineRunFinishEvent;
import io.datatok.djobi.engine.events.PipelineRunStartEvent;
import io.datatok.djobi.event.EventBus;
import io.datatok.djobi.plugins.report.subscribers.JobRunFinishSubscriber;
import io.datatok.djobi.plugins.report.subscribers.JobRunStartSubscriber;
import io.datatok.djobi.plugins.report.subscribers.PipelineRunFinishSubscriber;
import io.datatok.djobi.plugins.report.subscribers.PipelineRunStartSubscriber;
import io.datatok.djobi.utils.di.SimpleProviderFactory;

import javax.inject.Inject;

public class ReportingPlugin extends Plugin implements PluginBootstrap {

    @Inject
    private EventBus eventBus;

    @Inject
    private SimpleProviderFactory providerFactory;

    @Override
    public Class<? extends PluginBootstrap> getBootstrap() {
        return ReportingPlugin.class;
    }

    @Override
    public void bootstrap() {
        eventBus.subscribe(PipelineRunStartEvent.NAME, providerFactory.get(PipelineRunStartSubscriber.class));
        eventBus.subscribe(PipelineRunFinishEvent.NAME, providerFactory.get(PipelineRunFinishSubscriber.class));
        eventBus.subscribe(JobRunStartEvent.NAME, providerFactory.get(JobRunStartSubscriber.class));
        eventBus.subscribe(JobRunFinishEvent.NAME, providerFactory.get(JobRunFinishSubscriber.class));
    }

}
