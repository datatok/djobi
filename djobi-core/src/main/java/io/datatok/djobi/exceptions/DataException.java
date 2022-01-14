package io.datatok.djobi.exceptions;

import io.datatok.djobi.engine.stage.Stage;

public class DataException extends StageException {
    public DataException(Stage stage, String message) {
        super(stage, message);
    }
}
