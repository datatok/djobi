package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.stage.Stage;

public class StagePreCheckStartEvent extends StageAwareEvent {

    static final public String NAME = "stage-precheck-start";

    public StagePreCheckStartEvent(Stage stage) {
        super(stage);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
