package io.datatok.djobi.plugins.logging;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Workflow;
import io.datatok.djobi.engine.stage.Stage;

import javax.inject.Singleton;

@Singleton
public class LookupContext {

    private Workflow currentWorkflow;

    private Job currentJob;

    private Stage currentStage;

    public Workflow getCurrentPipeline() {
        return currentWorkflow;
    }

    public void setCurrentPipeline(Workflow currentWorkflow) {
        this.currentWorkflow = currentWorkflow;
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
