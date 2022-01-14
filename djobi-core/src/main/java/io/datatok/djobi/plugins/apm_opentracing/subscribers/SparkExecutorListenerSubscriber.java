package io.datatok.djobi.plugins.apm_opentracing.subscribers;

import co.elastic.apm.api.Span;
import io.datatok.djobi.event.Event;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.plugins.apm_opentracing.services.APMStore;
import io.datatok.djobi.spark.executor.SparkExecutorEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class SparkExecutorListenerSubscriber implements Subscriber {

    private Map<String, Span> jobToSpan = new HashMap<>();
    private Map<String, Span> taskToSpan = new HashMap<>();

    @Inject
    private APMStore store;

    @Override
    public void call(Event event) {

        if (this.store == null) {
            return ;
        }

        final SparkExecutorEvent e = (SparkExecutorEvent) event;

        switch (e.getEvent()) {
            case "onJobStart":
                this.onJobStart(e.getData());
            break;
            case "onJobEnd":
                this.onJobEnd(e.getData());
            break;
            case "onStageSubmitted":
                this.onStageSubmitted(e.getData());
            break;
            case "onStageCompleted":
                this.onStageCompleted(e.getData());
            break;
        }

    }

    public void onJobStart(Map<String, String> data) {
        final String jobId = data.get("id");
        //final Transaction djobiJobTransaction = this.store.getTransactionByJob();
        //final Map<String, String> headers = MyMapUtils.mapString("elastic-apm-traceparent",.getTraceId());

        final Span span = this.store.getTransactionForCurrentJob().startSpan("executor", "spark", "job");

        span
            .setName("Spark job " + jobId)
            .setLabel("executor", "spark")
        ;

        data.forEach(span::setLabel);

        this.jobToSpan.put(jobId, span);

        span.activate();
    }

    private void onJobEnd(Map<String, String> data) {
        final String jobId = data.get("id");
        final Span span = this.jobToSpan.get(jobId);

        if (span != null) {
            span.end();

            data.forEach(span::setLabel);

            this.jobToSpan.remove(jobId);
        }
    }

    public void onStageSubmitted(Map<String, String> data) {
        final String taskId = data.get("id");

        final Span span = this.store.getTransactionForCurrentJob()
                .startSpan("executor", "spark", "stage")
                .setName("Spark stage " + taskId)
                .setLabel("executor", "spark")
        ;

        data.forEach(span::setLabel);

        this.taskToSpan.put(taskId, span);

        span.activate();
    }

    private void onStageCompleted(Map<String, String> data) {
        final String taskId = data.get("id");
        final Span span = this.taskToSpan.get(taskId);

        if (span != null) {
            span.end();

            data.forEach(span::setLabel);

            this.taskToSpan.remove(taskId);
        }
    }
}
