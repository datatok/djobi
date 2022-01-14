package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.event.Event;

public abstract class JobAwareEvent extends Event {

    protected Job job;

    public JobAwareEvent(Job job) {
        this.job = job;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }
}
