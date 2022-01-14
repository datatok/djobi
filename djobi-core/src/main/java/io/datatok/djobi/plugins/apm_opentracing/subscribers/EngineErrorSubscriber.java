package io.datatok.djobi.plugins.apm_opentracing.subscribers;

import co.elastic.apm.api.Transaction;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.events.ErrorEvent;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.plugins.apm_opentracing.services.APMStore;

import javax.inject.Inject;

public class EngineErrorSubscriber  implements Subscriber {

    @Inject
    private APMStore store;

    @Override
    public void call(Event event) {
        final Object entity = ((ErrorEvent) event).getEntity();

        if (entity instanceof Stage) {
            final Job job = ((Stage) entity).getJob();

            if (job != null) {
                final Transaction transaction = this.store.getTransactionByJob(job);

                if (transaction != null) {
                    transaction.captureException(((ErrorEvent) event).getException());
                }
            }
        }
    }
}
