package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.event.Event;

public abstract class PipelineAwareEvent extends Event {

    private Pipeline pipeline;

    public PipelineAwareEvent(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }
}
