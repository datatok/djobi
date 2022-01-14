package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.Job;

public class JobPhaseFinishEvent extends JobPhaseAwareEvent {
    public static final String NAME = "job-phase-finish";

    public JobPhaseFinishEvent(Job job, String phase) {
        super(job, phase);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
