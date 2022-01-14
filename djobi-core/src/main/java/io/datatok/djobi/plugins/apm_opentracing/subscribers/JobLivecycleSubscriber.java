package io.datatok.djobi.plugins.apm_opentracing.subscribers;

import co.elastic.apm.api.Transaction;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.events.JobAwareEvent;
import io.datatok.djobi.engine.events.JobRunStartEvent;
import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.plugins.apm_opentracing.services.APMStore;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class JobLivecycleSubscriber implements Subscriber {

    @Inject
    private APMStore store;

    private Transaction currentTransaction;

    @Override
    public void call(Event event) {
        final Job job = ((JobAwareEvent) event).getJob();

        if (event.getName().equals(JobRunStartEvent.NAME)) {
            this.onStart(job);
        } else {
            this.onEnd(job);
        }
    }

    private void onStart(final Job job) {

        this.currentTransaction = this.store.getTransactionByJob(job);

        this.currentTransaction.ensureParentId();

        this.currentTransaction.activate();
    }

    private void onEnd(final Job job) {
        this.currentTransaction
            .setResult(job.getExecutionStatus().toString())
            .end();
        ;
    }
}
