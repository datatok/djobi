package io.datatok.djobi.spark.actions.sql;

import io.datatok.djobi.engine.actions.sql.SQLConfig;
import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionPreChecker;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import javax.inject.Inject;

public class  SparkSQLPreChecker implements ActionPreChecker {

    private static Logger logger = Logger.getLogger(SparkSQLPreChecker.class);

    @Inject
    protected TemplateUtils templateUtils;

    @Override
    public CheckResult preCheck(Stage stage) throws Exception {
        final SQLConfig configuration = (SQLConfig) stage.getParameters();

        if (configuration == null) {
            return CheckResult.error("configuration is null!");
        }

        if (configuration.check_path.isEmpty()) {
            return CheckResult.no().putMeta(CheckResult.REASON, "check_path is null!");
        }

        final String argFilePath = templateUtils.render(configuration.check_path, stage.getJob());
        final FileSystem hdfs = (FileSystem) stage.getJob().getWorkflow().getExecutor().get("hdfs");

        if (hdfs.exists(new Path(argFilePath))) {
            logger.info("[check] File at '" + argFilePath + "' exists!");

            return CheckResult.ok(CheckResult.REASON, "File at '" + argFilePath + "' exists!");
        }

        logger.error("[check] File at '" + argFilePath + "' does not exist!");

        return CheckResult.error("File at '" + argFilePath + "' does not exist!");
    }
}
