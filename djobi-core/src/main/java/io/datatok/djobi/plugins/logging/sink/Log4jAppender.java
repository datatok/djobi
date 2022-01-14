package io.datatok.djobi.plugins.logging.sink;

import io.datatok.djobi.plugins.logging.LoggerTypes;
import io.datatok.djobi.plugins.logging.LookupContext;
import io.datatok.djobi.utils.MyMapUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class Log4jAppender extends AppenderSkeleton {

    @Inject
    private LookupContext lookupContext;

    protected LogSink sink;

    protected final SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Inject
    public Log4jAppender(LoggerSinkFactory factory) {
        this.sink = factory.get(LoggerTypes.TYPE_LOGS);
    }

    @Override
    protected void append(LoggingEvent event) {
        try {
            final Map<String, Object> meta = new HashMap<>();

            if (lookupContext.getCurrentPipeline() != null) {
                meta.put("pipeline", lookupContext.getCurrentPipeline().toLog());
            }

            if (lookupContext.getCurrentJob() != null) {
                meta.put("job", lookupContext.getCurrentJob().toLog());
            }

            if (lookupContext.getCurrentStage() != null) {
                meta.put("stage", lookupContext.getCurrentStage().toLog());
            }

            this.sink.updateOrCreate(MyMapUtils.map(
        "@timestamp", dateParser.format(new Date(event.getTimeStamp())),
                "class", event.getFQNOfLoggerClass(),
                "logger", event.getLoggerName(),
                "level", event.getLevel().toString(),
                "message", event.getRenderedMessage(),
                "meta", meta
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
