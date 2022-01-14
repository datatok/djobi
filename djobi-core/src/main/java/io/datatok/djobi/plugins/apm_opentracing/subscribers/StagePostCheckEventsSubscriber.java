package io.datatok.djobi.plugins.apm_opentracing.subscribers;

import co.elastic.apm.api.ElasticApm;
import io.datatok.djobi.engine.events.StageAwareEvent;
import io.datatok.djobi.engine.events.StagePostCheckStartEvent;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.plugins.apm_opentracing.services.OpenTracingTracer;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StagePostCheckEventsSubscriber implements Subscriber {

    @Inject
    private OpenTracingTracer tracer;

    @Override
    public void call(Event event) {
        final Stage stage = ((StageAwareEvent) event).getStage();

        if (event instanceof StagePostCheckStartEvent) {
            ElasticApm
                .currentTransaction()
                .startSpan("engine", "stage", "post_check")
                .setName("stage post check " + stage.getName() + " [" + stage.getKind() + "]")
                .activate()
            ;
        } else {
            ElasticApm
                .currentSpan()
                .setLabel("status", stage.getPostCheck().getStatus().toString())
                .end();
            ;
        }
    }
}
