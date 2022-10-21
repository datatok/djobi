package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.Workflow;

public class PipelineRunFinishEvent extends PipelineAwareEvent {

    static final public String NAME = "pipeline-run-finish";

    public PipelineRunFinishEvent(Workflow workflow) {
        super(workflow);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
