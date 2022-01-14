package io.datatok.djobi.exceptions;

import io.datatok.djobi.engine.stage.Stage;

public class NetworkAccessException extends StageException {
    public NetworkAccessException(Stage stage, String message) {
        super(stage, message);
    }
}
