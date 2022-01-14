package io.datatok.djobi.exceptions;

import io.datatok.djobi.engine.stage.Stage;

public class StageException extends Exception {

    private Stage stage;

    public StageException(final Stage stage, final String message) {
        super(message);

        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public StageException setStage(Stage stage) {
        this.stage = stage;

        return this;
    }
}
