package io.datatok.djobi.spark.actions.fs;

import io.datatok.djobi.engine.actions.fs.output.FSOutputConfig;
import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionPostChecker;
import io.datatok.djobi.spark.executor.SparkExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import javax.inject.Inject;

public class SparkFSChecker implements ActionPostChecker {

    static private long SIZE_MIN = 50;

    static private String REASON_NOT_FOUND = "File not found";

    static private String REASON_TOO_SMALL = "File exists, but too small";

    @Inject
    private SparkExecutor sparkExecutor;

    /**
     * Check for Hadoop compatible FS (such as HDFS, S3).
     *
     * @param stage Stage
     * @return CheckResult
     * @throws Exception File exist fucked'up
     */
    @Override
    public CheckResult postCheck(Stage stage) throws Exception {
        final FSOutputConfig config = (FSOutputConfig) stage.getParameters();
        final FileSystem fs = sparkExecutor.getHDFS();
        final Path p = new Path(config.path);

        if (fs.exists(p)) {
            long l = fs.getContentSummary(p).getLength();

            if (l < SIZE_MIN) {
                return ActionPostChecker.error(REASON_TOO_SMALL);
            }

            return CheckResult.ok(
                    "display", FileUtils.byteCountToDisplaySize(l),
                    "value", l,
                    "unit", "byte"
            );
        }

        return ActionPostChecker.error("not found");
    }
}
