package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.stage.Stage;

public class StagePreCheckDoneEvent extends StageAwareEvent {

    static final public String NAME = "stage-precheck-done";

    public StagePreCheckDoneEvent(Stage stage) {
        super(stage);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
