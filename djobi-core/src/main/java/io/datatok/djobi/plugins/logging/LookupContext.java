package io.datatok.djobi.plugins.logging;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.engine.stage.Stage;

import javax.inject.Singleton;

@Singleton
public class LookupContext {

    private Pipeline currentPipeline;

    private Job currentJob;

    private Stage currentStage;

    public Pipeline getCurrentPipeline() {
        return currentPipeline;
    }

    public void setCurrentPipeline(Pipeline currentPipeline) {
        this.currentPipeline = currentPipeline;
    }

    public Job getCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(Job currentJob) {
        this.currentJob = currentJob;
    }

    public Stage getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }
}
