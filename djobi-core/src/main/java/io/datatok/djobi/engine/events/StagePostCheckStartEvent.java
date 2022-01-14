package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.stage.Stage;

public class StagePostCheckStartEvent extends StageAwareEvent {

    static final public String NAME = "stage-postcheck-start";

    public StagePostCheckStartEvent(Stage stage) {
        super(stage);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
