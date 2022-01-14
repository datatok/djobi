package io.datatok.djobi.engine.flow;

import com.google.inject.Singleton;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.executors.Executor;

@Singleton
public class ExecutionContext {

    private Executor executor;

    private StageData<?> stageData;

    private Job job;

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void setStageData(StageData<?> stageData) {
        this.stageData = stageData;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Executor getExecutor() {
        return executor;
    }

    public StageData<?> getStageData() {
        return stageData;
    }

    public StageData<?> getData() {
        return stageData;
    }

    public Job getJob() {
        return job;
    }
}
