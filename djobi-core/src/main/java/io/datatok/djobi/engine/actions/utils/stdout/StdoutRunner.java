package io.datatok.djobi.engine.actions.utils.stdout;

import com.google.inject.Inject;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.data.BasicDataKind;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.executors.ExecutorPool;
import io.datatok.djobi.plugins.report.Reporter;

import java.io.PrintStream;
import java.util.List;

public class StdoutRunner implements ActionRunner {

    @Inject
    private ExecutorPool executorPool;

    @Inject
    private Reporter reporter;

    @Override
    public ActionRunResult run(Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        final Job job = stage.getJob();

        final PrintStream outStream = reporter.getPrintStream();

        if (contextData == null) {
            outStream.println("Job data is null!");
            return ActionRunResult.fail("data is null!");
        } else {
            if (contextData.getKind().getType().equals(BasicDataKind.TYPE_LIST)) {

                for (Object item : ((List<?>) contextData.getData())) {
                    outStream.println(item);
                }
            }

            return ActionRunResult.success();
        }
    }

}
