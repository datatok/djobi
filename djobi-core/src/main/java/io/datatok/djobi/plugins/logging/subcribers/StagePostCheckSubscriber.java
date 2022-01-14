package io.datatok.djobi.plugins.logging.subcribers;

import io.datatok.djobi.engine.events.StageAwareEvent;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.plugins.logging.sink.LoggerSinkFactory;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StagePostCheckSubscriber extends StageAwareSubscriber implements Subscriber {

    static Logger logger = Logger.getLogger(StagePostCheckSubscriber.class);

    @Inject
    public StagePostCheckSubscriber(LoggerSinkFactory factory) {
        super(factory);

        logger.info(String.format("Initialize logger with sink %s", sink.getClass().toString()));
    }

    @Override
    public void call(Event event) {
        final Stage stage = ((StageAwareEvent) event).getStage();

        try {
            this.sink.updateOrCreate(stage.getUid(), resolveDocument(stage));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
