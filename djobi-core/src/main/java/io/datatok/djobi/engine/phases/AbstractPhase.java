package io.datatok.djobi.engine.phases;

import com.google.inject.Inject;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.ActionFactory;
import io.datatok.djobi.event.EventBus;
import io.datatok.djobi.plugins.logging.LookupContext;
import io.datatok.djobi.plugins.logging.feeders.DriverMetricsResource;
import io.datatok.djobi.plugins.report.OutVerbosity;
import io.datatok.djobi.plugins.report.Reporter;
import io.datatok.djobi.utils.QwObjects;

import java.util.Objects;

abstract public class AbstractPhase {

    @Inject
    protected EventBus eventBus;

    @Inject
    protected ActionFactory actionFactory;

    @Inject
    protected DriverMetricsResource driverMetricsResource;

    @Inject
    protected LookupContext lookupContext;

    @Inject(optional=true)
    protected OutVerbosity outVerbosity;

    @Inject(optional=true)
    protected Reporter reporter;

    abstract  public void execute(final Job job);

    /**
     * Report exception.
     * @since v3.2.0
     *
     * @param e
     */
    protected void reportException(Exception e) {
        if (Objects.nonNull(outVerbosity) && outVerbosity.isVeryVerbose()) {
            e.printStackTrace();
        } else if (QwObjects.nonNull(outVerbosity, reporter) && outVerbosity.isNotQuiet()) {
            e.printStackTrace();
            reporter.output("work logger error: %s", e.getMessage());
        }
    }

}
