package io.datatok.djobi.exceptions;

import io.datatok.djobi.engine.stage.Stage;

public class NotFoundException extends StageException {
    public NotFoundException(final Stage stage, final String message) {
        super(stage, message);
    }
}
