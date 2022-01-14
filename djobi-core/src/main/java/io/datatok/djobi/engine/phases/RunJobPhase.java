package io.datatok.djobi.engine.phases;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.enums.ExecutionStatus;
import io.datatok.djobi.engine.events.ErrorEvent;
import io.datatok.djobi.engine.events.StageRunFinishEvent;
import io.datatok.djobi.engine.events.StageRunStartEvent;
import io.datatok.djobi.engine.flow.ConditionFlow;
import io.datatok.djobi.engine.flow.ExecutionContext;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.event.EventBus;
import io.datatok.djobi.exceptions.StageException;
import org.apache.log4j.Logger;

import javax.inject.Inject;

public class RunJobPhase extends AbstractPhase {

    static Logger logger = Logger.getLogger(RunJobPhase.class);

    @Inject
    private ExecutionContext executionContext;

    @Inject
    private EventBus eventBus;

    @Inject
    private ConditionFlow conditionFlow;

    @Override
    public void execute(final Job job) {

        executionContext.setExecutor(job.getExecutor());
        executionContext.setJob(job);

        job.getStages()
            .stream()
            //.sorted(Comparator.comparingInt(s -> stagesOrder.indexOf(s.getStage())))
            .filter(Stage::getEnabled)
            .filter(this::testCondition)
            .forEach(stage -> {
                if (job.getExecutionStatus().equals(ExecutionStatus.IN_PROGRESS)) {
                    run(stage);
                }
            });
    }

    private boolean testCondition(final Stage stage) {
        if (stage.getCondition() == null || stage.getCondition().isEmpty()) {
            return true;
        }

        return conditionFlow.test(stage);
    }

    /**
     * Run all job stages, synchronously.
     *
     * @param stage The stage to run
     */
    private ActionRunResult run(final Stage stage)
    {
        lookupContext.setCurrentStage(stage);

        final Job job = stage.getJob();
        final ActionRunner runner = actionFactory.getRunner(stage/*, executionContext*/);

        Exception caughtException = null;
        boolean forceAllowFailure = false;
        ActionRunResult runResult = null;

        stage.getExecutor().setCurrentStage(stage);

        eventBus.trigger(new StageRunStartEvent(stage));

        if (runner == null)
        {
            caughtException = new StageException(stage, String.format("Action \"%s\" runner is null!", stage.getKind()));
            forceAllowFailure = true;
            runResult = new ActionRunResult(ExecutionStatus.NO);
        }
        else
        {
            logger.info(String.format("Running [job:%s:%s]", stage.getKind(), stage.getName()));

            try
            {
                runResult = runner.run(stage, executionContext.getStageData(), executionContext.getExecutor());

                this.executionContext.setStageData(runResult.getData());

            } catch (Exception e) {
                caughtException = e;

                runResult = new ActionRunResult(ExecutionStatus.DONE_ERROR);

                eventBus.trigger(new ErrorEvent(e, stage));

                reportException(e);

                logger.error(String.format("[stage:%s:%s:%s] %s", stage.getKind(), "pipeline", stage.getName(), e.getMessage()), e);
            }
        }

        if (caughtException == null)
        {
            stage.end(true);
        }
        else
        {
            logger.error("Exception caught during stage execution", caughtException);

            stage
                .end(false)
                .getLog().setException(caughtException)
            ;

            if (forceAllowFailure || !stage.getAllowFailure())
            {
                job.end(false);
            }
        }

        stage.getMetrics().putAll(driverMetricsResource.getMap());

        eventBus.trigger(new StageRunFinishEvent(stage));

        return runResult;
    }

}
