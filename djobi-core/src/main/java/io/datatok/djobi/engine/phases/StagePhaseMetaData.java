package io.datatok.djobi.engine.phases;

import io.datatok.djobi.engine.stage.livecycle.Action;

public class StagePhaseMetaData {

    private final String phase;

    private final Action action;

    public StagePhaseMetaData(String phase, Action action) {
        this.phase = phase;
        this.action = action;
    }

    public String getPhase() {
        return phase;
    }

    public Action getAction() {
        return action;
    }
}
