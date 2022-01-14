package io.datatok.djobi.engine.actions.fs.input;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.data.StageDataListString;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.exceptions.NotFoundException;
import io.datatok.djobi.executors.Executor;
import org.apache.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;

/**
 * Open file in a file-system (local fs or hdfs).
 *
 * Configuration:
 * - path
 * - format (json, parquet, xml, csv...)
 *
 * Output:
 * - DataFrame
 * - SQL table
 */
public class FSInputRunner implements ActionRunner {

    private static Logger logger = Logger.getLogger(FSInputRunner.class);

    public ActionRunResult run(final Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        final Job job = stage.getJob();
        final FSInputConfig config = (FSInputConfig) stage.getParameters();

        logger.info(String.format("[config] %s", config.toString()));

        try {
            List<String> lines = Files.readAllLines(Paths.get(config.path));
            StageDataListString stageData = new StageDataListString(config.format, lines);

            return ActionRunResult.success(stageData);
        } catch(NoSuchFileException e) {
            throw new NotFoundException(stage, "input file " + config.path + " not found!");
        }
    }


}
