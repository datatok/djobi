package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.stage.Stage;

public class StageRunFinishEvent extends StageAwareEvent {

    static final public String NAME = "stage-run-finish";

    public StageRunFinishEvent(Stage stage) {
        super(stage);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
