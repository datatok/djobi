package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.Pipeline;

public class PipelineRunStartEvent extends PipelineAwareEvent {

    static final public String NAME = "pipeline-run-start";

    public PipelineRunStartEvent(Pipeline pipeline) {
        super(pipeline);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
