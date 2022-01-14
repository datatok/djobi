package io.datatok.djobi.plugins.report.subscribers;

import com.google.inject.Inject;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.events.JobAwareEvent;
import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.plugins.report.OutVerbosity;
import io.datatok.djobi.plugins.report.Reporter;
import io.datatok.djobi.utils.QwObjects;

public class JobRunStartSubscriber implements Subscriber {

    @Inject(optional=true)
    private OutVerbosity outVerbosity;

    @Inject(optional=true)
    private Reporter reporter;

    @Override
    public void call(Event event) {
        if (QwObjects.nonNull(outVerbosity, reporter) && outVerbosity.isNotQuiet()) {
            final Job job = ((JobAwareEvent) event).getJob();

            if (outVerbosity.isNotQuiet()) {
                reporter.output("\n@|bold,green Running job %s, %d stages|@", job.getId(), job.getStages().size());
            }

            if (outVerbosity.isVeryVerbose()) {
                reporter.output("Stages before configuration:");
                reporter.printSummary(job);
            }
        }
    }
}
