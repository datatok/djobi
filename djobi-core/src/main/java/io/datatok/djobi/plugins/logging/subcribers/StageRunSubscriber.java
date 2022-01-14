package io.datatok.djobi.plugins.logging.subcribers;

import io.datatok.djobi.engine.events.StageAwareEvent;
import io.datatok.djobi.engine.events.StageRunStartEvent;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.plugins.logging.sink.LoggerSinkFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.StringWriter;

@Singleton
public class StageRunSubscriber extends StageAwareSubscriber implements Subscriber {

    static Logger logger = Logger.getLogger(StageRunSubscriber.class);

    final private StringWriter logInfoWriter = new StringWriter();
    final private StringWriter logErrorWriter = new StringWriter();

    @Inject
    public StageRunSubscriber(LoggerSinkFactory factory) {
        super(factory);

        logger.info(String.format("Initialize logger with sink %s", sink.getClass().toString()));

        final WriterAppender appender = new WriterAppender(new PatternLayout("[%p] [%d{ISO8601}] %m%n"), logInfoWriter);
        final WriterAppender errorAppender = new WriterAppender(new PatternLayout("[%p] [%d{ISO8601}] %m%n"), logErrorWriter);

        appender.setName("djobi-reporter");
        appender.setThreshold(Level.INFO);

        //errorAppender.setName("djobi-error");
        //errorAppender.setThreshold(Level.ERROR);

        //Logger.getRootLogger().addAppender(appender);

        //org.apache.log4j.Logger.getLogger("io.datatok.djobi").addAppender(appender);
        //org.apache.log4j.Logger.getLogger("io.datatok.djobi").setAdditivity(true);
    }

    @Override
    public void call(Event event) {
        final Stage stage = ((StageAwareEvent) event).getStage();

        if (event instanceof StageRunStartEvent) {
            this.start(stage);
        } else {
            this.end(stage);
        }
    }

    public void start(final Stage stage) {
        try {
            this.sink.updateOrCreate(stage.getUid(), resolveDocument(stage));
        } catch (Exception e) {
            logger.error("sink", e);
        }
    }

    public void end(final Stage stage) {
        final String logInfos = logInfoWriter.getBuffer().toString();
        final String logErrors = logErrorWriter.getBuffer().toString();

        logInfoWriter.getBuffer().delete(0, logInfoWriter.getBuffer().length());
        logErrorWriter.getBuffer().delete(0, logErrorWriter.getBuffer().length());

        stage.getLog().setRawLogs(logInfos);

        try {
            this.sink.updateOrCreate(stage.getUid(), resolveDocument(stage));
        } catch (Exception e) {
            logger.error("sink", e);
        }
    }


}
