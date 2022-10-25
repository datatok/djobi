package io.datatok.djobi.engine;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.datatok.djobi.engine.check.CheckStatus;
import io.datatok.djobi.engine.enums.ExecutionStatus;
import io.datatok.djobi.engine.events.*;
import io.datatok.djobi.engine.phases.AbstractPhase;
import io.datatok.djobi.engine.phases.ActionPhases;
import io.datatok.djobi.event.EventBus;
import io.datatok.djobi.plugins.logging.LookupContext;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

@Singleton
public class Engine {

    static Logger logger = Logger.getLogger(Engine.class);

    @Inject
    private EventBus eventBus;

    @Inject
    private LookupContext lookupContext;

    @Inject
    private Map<String, AbstractPhase> phases;

    /**
     * @since v3.6.0
     */
    private boolean clearJobAfterExecution = true;

    /**
     * Run the workflow.
     *
     * @param workflow workflow to run
     */
    public void run(final Workflow workflow) throws Exception {
        run(workflow, null);
    }

    public void run(final Workflow workflow, String jobIdFilter) throws Exception {
        lookupContext.setCurrentPipeline(workflow);

        logger.info(String.format("Run pipeline with %d jobs", workflow.getJobs().size()));

        this.eventBus.trigger(new PipelineRunStartEvent(workflow));

        for (Job job : workflow.getJobs()) {
            if (jobIdFilter == null || job.getId().equals(jobIdFilter)) {
                run(job);

                if (clearJobAfterExecution) {
                    job
                            .setData(null)
                            .setParameters(null)
                    ;
                }
            }
        }

        this.eventBus.trigger(new PipelineRunFinishEvent(workflow));
    }

    public void run(final Job job) throws Exception {
        lookupContext.setCurrentJob(job);

        final Workflow workflow = job.getWorkflow();

        if (workflow.getExecutor() != null) {
            workflow.getExecutor().connect();
        }

        this.eventBus.trigger(new JobRunStartEvent(job));

        job.start();

        logger.info(String.format("Executing job [%s]", job.getId()));

        final List<String> phasesFilter = workflow.getExecutionRequest().getJobPhases();

        this.runJobPhase(job, ActionPhases.CONFIGURE);

        if (phasesFilter.contains(ActionPhases.PRE_CHECK)) {
            this.runJobPhase(job, ActionPhases.PRE_CHECK);
        } else {
            job.setPreCheckStatus(CheckStatus.SKIPPED);
        }

        if (       job.getPreCheckStatus().equals(CheckStatus.DONE_OK)
                || job.getPreCheckStatus().equals(CheckStatus.NO)
                || job.getPreCheckStatus().equals(CheckStatus.SKIPPED)
        ) {
            if (phasesFilter.contains(ActionPhases.RUN)) {
                this.runJobPhase(job, ActionPhases.RUN);
            } else {
                job.setExecutionStatus(ExecutionStatus.SKIPPED);
            }

            if (phasesFilter.contains(ActionPhases.POST_CHECK)) {
                this.runJobPhase(job, ActionPhases.POST_CHECK);
            } else {
                job.setPostCheckStatus(CheckStatus.SKIPPED);
            }

            if (job.getExecutionStatus().equals(ExecutionStatus.IN_PROGRESS)) {
                job.end(true);
            }
        } else {
            job.end(false);
        }

        this.eventBus.trigger(new JobRunFinishEvent(job));
    }

    public boolean isClearJobAfterExecution() {
        return clearJobAfterExecution;
    }

    public void setClearJobAfterExecution(boolean clearJobAfterExecution) {
        this.clearJobAfterExecution = clearJobAfterExecution;
    }

    private void runJobPhase(final Job job, final String phase) {
        this.eventBus.trigger(new JobPhaseStartEvent(job, phase));
        this.phases.get(phase).execute(job);
        this.eventBus.trigger(new JobPhaseFinishEvent(job, phase));
    }

    /**
     * Report exception.
     * @since v3.2.0
     *
     * @param e
     */
    /*private void reportException(Exception e) {
        if (Objects.nonNull(outVerbosity) && outVerbosity.isVeryVerbose()) {
            e.printStackTrace();
        } else if (QwObjects.nonNull(outVerbosity, reporter) && outVerbosity.isNotQuiet()) {
            reporter.output("work logger error: " + e.getMessage());
        }
    }*/
}
