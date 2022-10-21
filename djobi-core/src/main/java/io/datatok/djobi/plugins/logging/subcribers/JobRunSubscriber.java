package io.datatok.djobi.plugins.logging.subcribers;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.events.JobAwareEvent;
import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.plugins.logging.LoggerTypes;
import io.datatok.djobi.plugins.logging.loggers.BaseLogger;
import io.datatok.djobi.plugins.logging.feeders.DriverMetricsResource;
import io.datatok.djobi.plugins.logging.sink.LoggerSinkFactory;
import io.datatok.djobi.plugins.logging.feeders.Transformer;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

/**
 * Listen "start" and "stop" job run events.
 */
public class JobRunSubscriber extends BaseLogger implements Subscriber {

    static Logger logger = Logger.getLogger(JobRunSubscriber.class);



    @Inject
    private Provider<DriverMetricsResource> driverMetricsResourceProvider;

    @Inject
    private Transformer transformer;

    @Inject
    public JobRunSubscriber(LoggerSinkFactory factory) {
        this.sink = factory.get(LoggerTypes.TYPE_JOBS);

        logger.info(String.format("Initialize logger with sink %s", sink.getClass().toString()));
    }

    @Override
    public void call(Event event) {
        final Job job = ((JobAwareEvent) event).getJob();

        final Map<String, Object> data = this.transformer.transform(job);

        data.put("@timestamp", dateParser.format(new java.util.Date()));
        data.put("date", dateParser.format(new java.util.Date()));

        try {
            sink.updateOrCreate(job.getUid(), data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
