package io.datatok.djobi.spark.executor;

import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.event.EventBus;
import io.datatok.djobi.executors.events.MetricAvailableEvent;
import io.datatok.djobi.utils.MyMapUtils;
import org.apache.spark.executor.InputMetrics;
import org.apache.spark.executor.OutputMetrics;
import org.apache.spark.executor.ShuffleReadMetrics;
import org.apache.spark.executor.TaskMetrics;
import org.apache.spark.scheduler.SparkListener;
import org.apache.spark.scheduler.SparkListenerApplicationEnd;
import org.apache.spark.scheduler.SparkListenerApplicationStart;
import org.apache.spark.scheduler.SparkListenerBlockManagerAdded;
import org.apache.spark.scheduler.SparkListenerBlockManagerRemoved;
import org.apache.spark.scheduler.SparkListenerBlockUpdated;
import org.apache.spark.scheduler.SparkListenerEnvironmentUpdate;
import org.apache.spark.scheduler.SparkListenerExecutorAdded;
import org.apache.spark.scheduler.SparkListenerExecutorMetricsUpdate;
import org.apache.spark.scheduler.SparkListenerExecutorRemoved;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.scheduler.SparkListenerJobStart;
import org.apache.spark.scheduler.SparkListenerStageCompleted;
import org.apache.spark.scheduler.SparkListenerStageSubmitted;
import org.apache.spark.scheduler.SparkListenerTaskEnd;
import org.apache.spark.scheduler.SparkListenerTaskGettingResult;
import org.apache.spark.scheduler.SparkListenerTaskStart;
import org.apache.spark.scheduler.SparkListenerUnpersistRDD;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.Map;

@Singleton
public class SparkReporter extends SparkListener {

    @Inject
    private EventBus eventBus;

    @Inject
    private SparkExecutorConfig configuration;

    private SparkListenerJobStart currentJob;

    private Stage currentStage;

    /**
     * Current Spark application ID
     */
    private String currentApplicationID;

    public SparkListenerJobStart getCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(SparkListenerJobStart currentJob) {
        this.currentJob = currentJob;
    }

    public Stage getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    public String getCurrentApplicationID() {
        return currentApplicationID;
    }

    public void setCurrentApplicationID(String currentApplicationID) {
        this.currentApplicationID = currentApplicationID;
    }

    @Override
    public void onStageCompleted(SparkListenerStageCompleted stageCompleted) {
        this.eventBus.trigger(SparkExecutorEvent.build("onStageCompleted").addMeta("id", "" + stageCompleted.stageInfo().stageId()));
    }

    @Override
    public void onBlockUpdated(SparkListenerBlockUpdated blockUpdated) {

    }

    @Override
    public void onJobStart(SparkListenerJobStart jobStart) {
        this.currentJob = jobStart;

        this.eventBus.trigger(
                SparkExecutorEvent.build("onJobStart")
                        .addMeta("id", "" + jobStart.jobId())
                        .addMeta("history_url", getWebHistoryUrlForJob("" + jobStart.jobId()))
        );
    }

    @Override
    public void onStageSubmitted(SparkListenerStageSubmitted stageSubmitted) {
        this.eventBus.trigger(
                SparkExecutorEvent.build("onStageSubmitted")
                        .addMeta("id", "" + stageSubmitted.stageInfo().stageId())
                        .addMeta("history_url", getWebHistoryUrlForJob("" + stageSubmitted.stageInfo().stageId()))
        );
    }

    @Override
    public void onTaskGettingResult(SparkListenerTaskGettingResult jobGettingResult) {

    }

    @Override
    public void onBlockManagerRemoved(SparkListenerBlockManagerRemoved blockManagerRemoved) {

    }

    @Override
    public void onApplicationStart(SparkListenerApplicationStart applicationStart) {

    }

    @Override
    public void onUnpersistRDD(SparkListenerUnpersistRDD unpersistRDD) {

    }

    @Override
    public void onExecutorAdded(SparkListenerExecutorAdded executorAdded) {

    }

    @Override
    public void onEnvironmentUpdate(SparkListenerEnvironmentUpdate environmentUpdate) {

    }

    @Override
    public void onExecutorMetricsUpdate(SparkListenerExecutorMetricsUpdate executorMetricsUpdate) {

    }

    @Override
    public void onExecutorRemoved(SparkListenerExecutorRemoved executorRemoved) {

    }

    @Override
    public void onTaskEnd(SparkListenerTaskEnd jobEnd) {
        final TaskMetrics taskMetrics = jobEnd.taskMetrics();

        Map<String, Object> metrics;

        if (taskMetrics == null) {
            this.eventBus.trigger(new SparkExecutorEvent("onTaskEnd", MyMapUtils.mapString(
                    "task_id", jobEnd.taskInfo().id()
            )));

            metrics = MyMapUtils.map(
                    "id", jobEnd.taskInfo().id(),
                    "timestamp", new Date().getTime(),
                    "executor", MyMapUtils.map(
                            "id", jobEnd.taskInfo().executorId(),
                            "host", jobEnd.taskInfo().host(),
                            "locality", jobEnd.taskInfo().taskLocality().toString()
                    )
            );
        } else {
            this.eventBus.trigger(new SparkExecutorEvent("onTaskEnd", MyMapUtils.mapString(
                    "task_id", jobEnd.taskInfo().id(),
                    "run_time", taskMetrics.executorRunTime() + " ms",
                    "gc_time", taskMetrics.jvmGCTime() + " ms",
                    "memory_used", taskMetrics.memoryBytesSpilled() + " o",
                    "disk_used", taskMetrics.diskBytesSpilled() + " o",
                    "result_size", taskMetrics.resultSize() + ""
            )));

            metrics = MyMapUtils.map(
                    "task_id", jobEnd.taskInfo().id(),
                    "job_id", "" + (currentJob == null ? 0 : currentJob.jobId()),
                    "stage_id","" + jobEnd.stageId(),
                    "status", jobEnd.taskInfo().status(),
                    "timestamp", new Date().getTime(),
                    "run_time", taskMetrics.executorRunTime(),
                    "deserialize_time", taskMetrics.executorDeserializeTime(),
                    "gc_time", taskMetrics.jvmGCTime(),
                    "memory_used", taskMetrics.memoryBytesSpilled(),
                    "disk_used", taskMetrics.diskBytesSpilled(),
                    "result_size", taskMetrics.resultSize(),
                    "executor", MyMapUtils.map(
                            "id", jobEnd.taskInfo().executorId(),
                            "host", jobEnd.taskInfo().host(),
                            "locality", jobEnd.taskInfo().taskLocality().toString()
                    )
            );

            final ShuffleReadMetrics shuffleReadMetrics = taskMetrics.shuffleReadMetrics();
            final InputMetrics inputMetrics = taskMetrics.inputMetrics();
            final OutputMetrics outputMetrics = taskMetrics.outputMetrics();

            metrics.put("shuffle", MyMapUtils.map(
                    "fetch_wait_time", shuffleReadMetrics.fetchWaitTime(),
                    "total_byte_read", shuffleReadMetrics.totalBytesRead(),
                    "remote_byte_read", shuffleReadMetrics.remoteBytesRead(),
                    "local_byte_read", shuffleReadMetrics.localBytesRead()
            ));


                metrics.put("input", MyMapUtils.map(
                        "bytes", inputMetrics.bytesRead(),
                        "records", inputMetrics.recordsRead()
                ));


                metrics.put("output", MyMapUtils.map(
                        "bytes", outputMetrics.bytesWritten(),
                        "records", outputMetrics.recordsWritten()
                ));

        }

        //logger.debug(String.format("spark task end %s", jobEnd.taskInfo().id()));

        if (this.currentStage != null) {
            this.eventBus.trigger(new MetricAvailableEvent(this.currentStage, metrics));
        }
    }

    @Override
    public void onTaskStart(SparkListenerTaskStart jobStart) {

    }

    @Override
    public void onBlockManagerAdded(SparkListenerBlockManagerAdded blockManagerAdded) {

    }

    @Override
    public void onJobEnd(SparkListenerJobEnd jobEnd) {
        this.eventBus.trigger(SparkExecutorEvent
                .build("onJobEnd")
                .addMeta("id", jobEnd.jobId() + "")
                .addMeta("result", jobEnd.jobResult().toString())
        );
    }

    @Override
    public void onApplicationEnd(SparkListenerApplicationEnd applicationEnd) {
        //logInfoWriter.flush();

        //new LogReport().send(sparkContext, logInfoWriter.getBuffer().toString(), logErrorWriter.getBuffer().toString());

        //workLogger.send
    }


    private String getWebHistoryUrlForJob(final String id) {
        return this.configuration.getWebHistoryUrlForJob()
                .replace("{{app_id}}", currentApplicationID)
                .replace("{{id}}", id)
                ;
    }

}
