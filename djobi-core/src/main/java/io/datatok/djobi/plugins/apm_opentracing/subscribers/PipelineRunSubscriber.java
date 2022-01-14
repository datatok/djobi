package io.datatok.djobi.plugins.apm_opentracing.subscribers;

import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.engine.events.PipelineAwareEvent;
import io.datatok.djobi.engine.events.PipelineRunStartEvent;
import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.plugins.apm_opentracing.services.APMStore;

import javax.inject.Inject;

public class PipelineRunSubscriber implements Subscriber {

    @Inject
    private APMStore store;

    @Override
    public void call(Event event) {
        final Pipeline pipeline = ((PipelineAwareEvent) event).getPipeline();

        if (event.getName().equals(PipelineRunStartEvent.NAME)) {
            onStart(pipeline);
        } else {
            onEnd(pipeline);
        }
    }

    private void onStart(final Pipeline pipeline) {
        //final Transaction t = this.store.getTransactionByPipeline(pipeline);

        //t.activate();
    }


    private void onEnd(final Pipeline pipeline) {
        //final Transaction t = this.store.getTransactionByPipeline(pipeline);

        //t.end();
    }
}
