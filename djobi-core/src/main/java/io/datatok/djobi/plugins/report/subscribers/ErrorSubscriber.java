package io.datatok.djobi.plugins.report.subscribers;

import com.google.inject.Inject;
import io.datatok.djobi.engine.events.ErrorEvent;
import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.plugins.report.OutVerbosity;
import io.datatok.djobi.plugins.report.Reporter;

public class ErrorSubscriber implements Subscriber {
    @Inject(optional=true)
    private OutVerbosity outVerbosity;

    @Inject(optional=true)
    private Reporter reporter;

    @Override
    public void call(Event event) {
        reporter.error( ((ErrorEvent) event).getException());
    }
}
