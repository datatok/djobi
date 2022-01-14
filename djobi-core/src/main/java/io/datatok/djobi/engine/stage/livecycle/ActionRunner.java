package io.datatok.djobi.engine.stage.livecycle;

import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.executors.Executor;

public interface ActionRunner extends Action {

    /**
     * Worker could not accept the job, if job data is not compatible.
     *
     * @since v2.1.0
     * @param job Job
     * @return boolean
     */
    //Boolean accept(final Job job);

    /**
     * Run the worker.
     *
     * @since v0.1.0
     * @param stage Job
     * @param contextData
     * @param contextExecutor
     * @throws Exception
     * @return
     */
    ActionRunResult run(final Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception;
}
