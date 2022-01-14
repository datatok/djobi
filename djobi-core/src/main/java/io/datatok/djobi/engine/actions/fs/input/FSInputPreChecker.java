package io.datatok.djobi.engine.actions.fs.input;

import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionPreChecker;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

public class FSInputPreChecker implements ActionPreChecker {

    private static final Logger logger = Logger.getLogger(FSInputPreChecker.class);

    @Override
    public CheckResult preCheck(Stage stage) throws Exception {
        final FSInputConfig configuration = (FSInputConfig) stage.getParameters();

        if (configuration == null) {
            return CheckResult.error("configuration is null!");
        }

        if (configuration.path.isEmpty()) {
            return CheckResult.no().putMeta(CheckResult.REASON, "check_path is null!");
        }

        final String argFilePath = configuration.path;
        final FileSystem hdfs = (FileSystem) stage.getJob().getPipeline().getExecutor().get("hdfs");

        if (hdfs.exists(new Path(argFilePath))) {
            logger.info("[check] File at '" + argFilePath + "' exists!");

            return CheckResult.ok(CheckResult.REASON, "File at '" + argFilePath + "' exists!");
        }

        logger.error("[check] File at '" + argFilePath + "' does not exist!");

        return CheckResult.error("File at '" + argFilePath + "' does not exist!");
    }
}
