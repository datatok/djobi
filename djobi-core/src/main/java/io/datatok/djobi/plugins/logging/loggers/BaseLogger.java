package io.datatok.djobi.plugins.logging.loggers;

import io.datatok.djobi.application.ApplicationData;
import io.datatok.djobi.plugins.logging.sink.LogSink;
import io.datatok.djobi.utils.MyMapUtils;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Map;

abstract public class BaseLogger {

    protected LogSink sink;

    protected final SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Inject
    /**
     * @since v3.3.1
     */
    private ApplicationData runData;

    public LogSink getSink() {
        return this.sink;
    }

    /**
     * Fill agent (djobi).
     *
     * @param eventData Map<String, Object>
     */
    protected void fillAgentData(final Map<String, Object> eventData) {
        eventData.put("agent", MyMapUtils.mapString(
           "name", "djobi",
                "version",  runData.getVersion(),
                "type", "application"
        ));
    }

    /**
     * Fill ECS.
     *
     * @param eventData Map<String, Object>
     */
    protected void fillECSData(final Map<String, Object> eventData) {
        eventData.put("ecs", MyMapUtils.mapString(
                "version",  "1.0.0"
        ));
    }

    /**
     * Fill error fields.
     *
     * @param eventData Map<String, Object>
     */
    protected void fillErrorFields(final Map<String, Object> eventData, Exception e) {
        if (e != null) {
            eventData.put("error", MyMapUtils.map(
               "message", e.getMessage(),
               "code", "exception",
                "type", e.getClass().getCanonicalName(),
                "stack_trace", Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).reduce("", (x, y) -> x + y + ",\n")
            ));
        }
    }

}
