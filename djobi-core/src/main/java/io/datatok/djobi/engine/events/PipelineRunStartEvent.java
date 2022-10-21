package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.Workflow;

public class PipelineRunStartEvent extends PipelineAwareEvent {

    static final public String NAME = "pipeline-run-start";

    public PipelineRunStartEvent(Workflow workflow) {
        super(workflow);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
