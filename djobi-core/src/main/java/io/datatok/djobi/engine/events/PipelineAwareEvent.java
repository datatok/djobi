package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.Workflow;
import io.datatok.djobi.event.Event;

public abstract class PipelineAwareEvent extends Event {

    private Workflow workflow;

    public PipelineAwareEvent(Workflow workflow) {
        this.workflow = workflow;
    }

    public Workflow getPipeline() {
        return workflow;
    }
}
