package io.datatok.djobi.engine.events;

import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.event.Event;

public abstract class StageAwareEvent extends Event {

    protected Stage stage;

    public StageAwareEvent(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}
