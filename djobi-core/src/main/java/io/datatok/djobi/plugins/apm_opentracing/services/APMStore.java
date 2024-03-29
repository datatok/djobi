package io.datatok.djobi.plugins.apm_opentracing.services;

import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Transaction;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Workflow;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class APMStore {

    private Map<String, Transaction> transactions = new HashMap<>();

    private Transaction currentJobTransaction;

    /**
     * Hold job transaction.
     *
     * @param workflow Pipeline
     * @return Transaction
     */
    public Transaction getTransactionByPipeline(final Workflow workflow) {
        final String k = "pipeline-" + workflow.getUid();

        if (!this.transactions.containsKey(k)) {
            final Transaction t = ElasticApm
                    .startTransaction()
                    .setName(workflow.getName())
                    .setLabel("pipeline_name", workflow.getName())
                    .setLabel("pipeline_uid", workflow.getUid())
            ;

            t.setLabel("id", t.getId());
            t.setLabel("trace_id", t.getTraceId());

            this.transactions.put(k, t);
        }

        return this.transactions.get(k);
    }

    /**
     * Hold job transaction.
     *
     * @param job Job
     * @return Transaction
     */
    public Transaction getTransactionByJob(final Job job) {
        final String k = "job-" + job.getUid();

        if (!this.transactions.containsKey(k)) {
            //final Transaction pipelineTransaction = this.getTransactionByPipeline(job.getPipeline());
            //final Map<String, String> headers = MyMapUtils.mapString("elastic-apm-traceparent", pipelineTransaction.getTraceId());

            final Transaction t = ElasticApm
                    .startTransaction()
                        .setName(job.getWorkflow().getName() + " - " + job.getId())
                        .setLabel("pipeline", job.getWorkflow().getName())
                        .setLabel("uid", job.getUid())
            ;

            job.setMeta(Job.META_APM_TRACE_ID, t.getTraceId());

            this.transactions.put(k, t);
        }

        this.currentJobTransaction = this.transactions.get(k);

        return this.currentJobTransaction;
    }

    public Transaction getTransactionForCurrentJob() {
        return this.currentJobTransaction;
    }

}
