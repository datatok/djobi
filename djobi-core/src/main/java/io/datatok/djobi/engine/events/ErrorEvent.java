package io.datatok.djobi.engine.events;

import io.datatok.djobi.event.Event;

public class ErrorEvent extends Event {

    static final public String NAME = "engine-error";

    private Throwable exception;

    private Object entity;

    public ErrorEvent(Throwable exception, Object entity) {
        this.exception = exception;
        this.entity = entity;
    }

    public Throwable getException() {
        return exception;
    }

    public Object getEntity() {
        return entity;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
