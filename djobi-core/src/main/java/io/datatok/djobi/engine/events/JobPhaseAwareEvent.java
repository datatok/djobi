package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.Job;

public abstract class JobPhaseAwareEvent extends JobAwareEvent {
    protected String phase;

    public JobPhaseAwareEvent(Job job, String phase) {
        super(job);

        this.phase = phase;
    }

    public String getPhase() {
        return phase;
    }
}
