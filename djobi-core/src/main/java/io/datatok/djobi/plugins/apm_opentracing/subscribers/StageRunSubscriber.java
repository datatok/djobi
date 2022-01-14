package io.datatok.djobi.plugins.apm_opentracing.subscribers;

import co.elastic.apm.api.Span;
import co.elastic.apm.api.Transaction;
import io.datatok.djobi.engine.events.StageAwareEvent;
import io.datatok.djobi.engine.events.StageRunStartEvent;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.plugins.apm_opentracing.services.APMStore;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StageRunSubscriber implements Subscriber {

    @Inject
    private APMStore store;

    private Span currentSpan;

    @Override
    public void call(Event event) {
        final Stage stage = ((StageAwareEvent) event).getStage();

        if (event.getName().equals(StageRunStartEvent.NAME)) {
            onStart(stage);
        } else {
            onEnd(stage);
        }
    }

    private void onStart(final Stage stage) {
        if (this.currentSpan != null) {
            this.currentSpan.end();
        }

        final Transaction jobTransaction = this.store.getTransactionByJob(stage.getJob());

        this.currentSpan = jobTransaction
                .startSpan("engine", "stage", "run")
                .setName("run stage " + stage.getName())
        ;

        this.currentSpan.activate();
    }


    private void onEnd(final Stage stage) {
        if (this.currentSpan != null) {
            this.currentSpan
                    .setLabel("execution_status", stage.getExecutionStatus().toString())
                    .end();
        }

        this.currentSpan = null;
    }
}
