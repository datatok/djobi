package io.datatok.djobi.plugins.logging.subcribers;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.events.JobRunFinishEvent;
import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.plugins.logging.LoggerTypes;
import io.datatok.djobi.plugins.logging.loggers.BaseLogger;
import io.datatok.djobi.plugins.logging.resources.DriverLogResource;
import io.datatok.djobi.plugins.logging.resources.DriverMetricsResource;
import io.datatok.djobi.plugins.logging.sink.LoggerSinkFactory;
import io.datatok.djobi.utils.MyMapUtils;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

public class JobRunFinishSubscriber extends BaseLogger implements Subscriber {

    static Logger logger = Logger.getLogger(JobRunFinishSubscriber.class);

    @Inject
    private Provider<DriverLogResource> driverLogResourceProvider;

    @Inject
    private Provider<DriverMetricsResource> driverMetricsResourceProvider;

    @Inject
    public JobRunFinishSubscriber(LoggerSinkFactory factory) {
        this.sink = factory.get(LoggerTypes.TYPE_JOBS);

        logger.info(String.format("Initialize logger with sink %s", sink.getClass().toString()));
    }

    @Override
    public void call(Event event) {
        final Job job = ((JobRunFinishEvent) event).getJob();

        final Map<String, Object> data = MyMapUtils.map(
                "timeline", job.getTimeline(),
                "run_status", job.getExecutionStatus().toString(),
                "pre_check_status", job.getPreCheckStatus().toString(),
                "post_check_status", job.getPostCheckStatus().toString(),
                "driver", MyMapUtils.map("metrics", driverMetricsResourceProvider.get().getMap())
        );

        if (job.getMeta().containsKey(Job.META_APM_TRACE_ID)) {
            data.put(Job.META_APM_TRACE_ID, job.getMeta().get(Job.META_APM_TRACE_ID));
        }

        try {
            sink.updateAtomic(job.getUid(), data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
