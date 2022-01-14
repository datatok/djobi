package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.Job;

public class JobRunStartEvent extends JobAwareEvent {

    static public String NAME = "job-run-start";

    public JobRunStartEvent(Job job) {
        super(job);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
