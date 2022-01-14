package io.datatok.djobi.plugins.report.subscribers;

import com.google.inject.Inject;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.events.JobAwareEvent;
import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.plugins.report.OutVerbosity;
import io.datatok.djobi.plugins.report.Reporter;
import io.datatok.djobi.utils.QwObjects;

public class JobRunFinishSubscriber implements Subscriber {

    @Inject(optional=true)
    private OutVerbosity outVerbosity;

    @Inject(optional=true)
    private Reporter reporter;

    @Override
    public void call(Event event) {
        if (QwObjects.nonNull(outVerbosity, reporter) && outVerbosity.isNotQuiet() && outVerbosity.isNotQuiet()) {
            final Job job = ((JobAwareEvent) event).getJob();

            switch (job.getExecutionStatus()) {
                case DONE_OK:
                    reporter.output(reporter.success(String.format("job execution %s : success!", job.getId())));
                    break;
                case DONE_ERROR:
                    reporter.output(reporter.error(String.format("job execution %s : error!", job.getId())));
                    break;
                default:
                    reporter.output(reporter.error(String.format("Job execution status unknown: %s !", job.getExecutionStatus())));
            }
        }
    }
}
