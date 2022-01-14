package io.datatok.djobi.plugins.report.subscribers;

import com.google.inject.Inject;
import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.engine.events.PipelineAwareEvent;
import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.plugins.report.OutVerbosity;
import io.datatok.djobi.plugins.report.Reporter;
import io.datatok.djobi.utils.QwObjects;

public class PipelineRunStartSubscriber implements Subscriber {

    @Inject(optional=true)
    private OutVerbosity outVerbosity;

    @Inject(optional=true)
    private Reporter reporter;

    @Override
    public void call(Event event) {
        if (QwObjects.nonNull(outVerbosity, reporter) && outVerbosity.isNotQuiet()) {
            final Pipeline pipeline = ((PipelineAwareEvent) event).getPipeline();

            if (QwObjects.nonNull(outVerbosity, reporter) && outVerbosity.isNotQuiet()) {
                reporter.output("\n\n@|bold,green,underline Running pipeline %s, %d jobs|@\n", pipeline.getName(), pipeline.getJobs().size());
            }
        }
    }
}
