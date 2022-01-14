package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.Pipeline;

public class PipelineRunFinishEvent extends PipelineAwareEvent {

    static final public String NAME = "pipeline-run-finish";

    public PipelineRunFinishEvent(Pipeline pipeline) {
        super(pipeline);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
