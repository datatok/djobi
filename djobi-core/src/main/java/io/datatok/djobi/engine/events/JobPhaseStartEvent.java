package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.Job;

public class JobPhaseStartEvent extends JobPhaseAwareEvent {
    public static final String NAME = "job-phase-start";

    public JobPhaseStartEvent(Job job, String phase) {
        super(job, phase);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
