package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.stage.Stage;

public class StageRunStartEvent extends StageAwareEvent {

    static final public String NAME = "stage-run-start";

    public StageRunStartEvent(Stage stage) {
        super(stage);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
