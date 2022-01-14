package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.Job;

public class JobRunFinishEvent extends JobAwareEvent {

    static final public String NAME = "job-run-finish";

    public JobRunFinishEvent(Job job) {
        super(job);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
