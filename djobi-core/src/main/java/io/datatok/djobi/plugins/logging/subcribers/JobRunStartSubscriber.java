package io.datatok.djobi.plugins.logging.subcribers;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.engine.events.JobRunStartEvent;
import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.plugins.logging.LoggerTypes;
import io.datatok.djobi.plugins.logging.loggers.BaseLogger;
import io.datatok.djobi.plugins.logging.resources.DriverLogResource;
import io.datatok.djobi.plugins.logging.resources.DriverMetricsResource;
import io.datatok.djobi.plugins.logging.sink.LoggerSinkFactory;
import io.datatok.djobi.utils.MyMapUtils;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

public class JobRunStartSubscriber extends BaseLogger implements Subscriber {

    static Logger logger = Logger.getLogger(JobRunStartSubscriber.class);

    @Inject
    private Provider<DriverLogResource> driverLogResourceProvider;

    @Inject
    private Provider<DriverMetricsResource> driverMetricsResourceProvider;

    @Inject
    public JobRunStartSubscriber(LoggerSinkFactory factory) {
        this.sink = factory.get(LoggerTypes.TYPE_JOBS);

        logger.info(String.format("Initialize logger with sink %s", sink.getClass().toString()));
    }

    @Override
    public void call(Event event) {
        final Job job = ((JobRunStartEvent) event).getJob();

        final Pipeline pipeline = job.getPipeline();

        final Map<String, Object> pipelineMap = MyMapUtils.map(
                "uid", pipeline.getUid(),
                "name", pipeline.getName(),
                "labels", pipeline.getLabels()
        );

        final Map<String, String> timelineMap = MyMapUtils.map(
                "start", Calendar.getInstance().getTime()
        );

        final Map<String, Object> data = MyMapUtils.map(
            "id", job.getId(),
            "uid", job.getUid(),
            "pre_check_status", job.getPreCheckStatus().toString(),
            "post_check_status", job.getPostCheckStatus().toString(),
            "args", job.getParameters(),
            "run_status", job.getExecutionStatus().toString(),
            "pipeline", pipelineMap,
            "date", dateParser.format(new java.util.Date()),
            "timeline", timelineMap,
            "meta", pipeline.getPipelineRequest().getMetaDataLabels(),
            "labels", job.getLabels()
        );

        driverLogResourceProvider.get().fillEventData(data);
        fillAgentData(data);
        fillECSData(data);

        if (job.getMeta().containsKey(Job.META_APM_TRACE_ID)) {
            data.put(Job.META_APM_TRACE_ID, job.getMeta().get(Job.META_APM_TRACE_ID));
        }

        final Executor executor = pipeline.getExecutor();

        if (executor != null) {
            try {
                data.put("executor", MyMapUtils.map(executor.getType(), executor.getMeta()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            sink.updateOrCreate(job.getUid(), data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
