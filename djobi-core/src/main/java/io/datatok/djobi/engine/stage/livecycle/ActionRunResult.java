package io.datatok.djobi.engine.stage.livecycle;

import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.enums.ExecutionStatus;
import io.datatok.djobi.utils.Timeline;

/**
 * Hold result of stage run.
 */
public class ActionRunResult {

    private ExecutionStatus status;

    private StageData<?> data;

    private Timeline timeline;

    static public ActionRunResult success(StageData<?> data)
    {
        return new ActionRunResult(ExecutionStatus.DONE_OK, data);
    }

    static public ActionRunResult success()
    {
        return new ActionRunResult(ExecutionStatus.DONE_OK);
    }

    static public ActionRunResult fail(String reason)
    {
        return new ActionRunResult(ExecutionStatus.DONE_ERROR);
    }

    public ActionRunResult(ExecutionStatus status, StageData<?> data) {
        this.status = status;
        this.data = data;
    }

    public ActionRunResult(ExecutionStatus status) {
        this.status = status;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public StageData<?> getData() {
        return data;
    }

    public Timeline getTimeline() {
        return timeline;
    }
}
