package io.datatok.djobi.event;

public interface Subscriber {

    public void call(final Event event);

}
