package io.datatok.djobi.plugins.logging.subcribers;

import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.executors.events.MetricAvailableEvent;
import io.datatok.djobi.plugins.logging.LoggerTypes;
import io.datatok.djobi.plugins.logging.loggers.BaseLogger;
import io.datatok.djobi.plugins.logging.sink.LoggerSinkFactory;
import io.datatok.djobi.utils.MyMapUtils;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Calendar;
import java.util.Map;

@Singleton
public class MetricsLogger extends BaseLogger implements Subscriber {
    static Logger logger = Logger.getLogger(MetricsLogger.class);

    @Inject
    public MetricsLogger(LoggerSinkFactory factory) {
        this.sink = factory.get(LoggerTypes.TYPE_METRICS);

        logger.info(String.format("Initialize logger with sink %s", sink.getClass().toString()));
    }

    @Override
    public void call(Event event) {
        final Stage stage = ((MetricAvailableEvent) event).getStage();
        final Map<String, Object> metrics = ((MetricAvailableEvent) event).getMetric();

        if (stage.getJob() != null && stage.getJob().getStages() != null) {
            metrics.put("_meta_", MyMapUtils.map(
                    "stage", stage.getUid(),
                    "job", stage.getJob().getUid(),
                    "pipeline", stage.getJob().getWorkflow().getName(),
                    "date", Calendar.getInstance().getTime()
            ));
        } else {
            metrics.put("_meta_", MyMapUtils.map(
                    "date", Calendar.getInstance().getTime()
            ));
        }

        try {
            this.sink.updateOrCreate(metrics);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
