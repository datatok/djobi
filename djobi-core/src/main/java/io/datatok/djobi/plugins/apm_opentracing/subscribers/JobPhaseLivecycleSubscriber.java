package io.datatok.djobi.plugins.apm_opentracing.subscribers;

import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Span;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.events.JobPhaseAwareEvent;
import io.datatok.djobi.engine.events.JobPhaseStartEvent;
import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;

public class JobPhaseLivecycleSubscriber implements Subscriber {
    private Span span;

    @Override
    public void call(Event event) {
        final Job job = ((JobPhaseAwareEvent) event).getJob();
        final String phase = ((JobPhaseAwareEvent) event).getPhase();

        if (event.getName().equals(JobPhaseStartEvent.NAME)) {
            this.onStart(job, phase);
        } else {
            this.onEnd(job, phase);
        }
    }

    private void onStart(final Job job, final String phase) {
        this.span = ElasticApm.currentTransaction().startSpan("djobi", "job", "run").setName("phase " + phase);
        this.span.activate();
    }

    private void onEnd(final Job job, final String phase) {
        if (this.span != null) {
            this.span.end();
        }
    }
}
