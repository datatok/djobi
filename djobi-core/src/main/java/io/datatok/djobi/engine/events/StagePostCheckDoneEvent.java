package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.stage.Stage;

public class StagePostCheckDoneEvent extends StageAwareEvent {

    static final public String NAME = "stage-postcheck-done";

    public StagePostCheckDoneEvent(Stage stage) {
        super(stage);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
