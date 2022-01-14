package io.datatok.djobi.plugins.logging.loggers;

import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.plugins.logging.sink.Log4jAppender;
import org.apache.log4j.Logger;

import javax.inject.Inject;

public class Log4jLogger implements Subscriber {

    @Inject
    private Log4jAppender appender;

    @Override
    public void call(Event event) {
        Logger.getRootLogger().setAdditivity(true);
        //Logger.getRootLogger().addAppender(appender);
    }
}
