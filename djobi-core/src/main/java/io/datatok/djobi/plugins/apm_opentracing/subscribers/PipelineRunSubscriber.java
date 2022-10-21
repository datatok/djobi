package io.datatok.djobi.plugins.apm_opentracing.subscribers;

import io.datatok.djobi.engine.Workflow;
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
        final Workflow workflow = ((PipelineAwareEvent) event).getPipeline();

        if (event.getName().equals(PipelineRunStartEvent.NAME)) {
            onStart(workflow);
        } else {
            onEnd(workflow);
        }
    }

    private void onStart(final Workflow workflow) {
        //final Transaction t = this.store.getTransactionByPipeline(pipeline);

        //t.activate();
    }


    private void onEnd(final Workflow workflow) {
        //final Transaction t = this.store.getTransactionByPipeline(pipeline);

        //t.end();
    }
}
