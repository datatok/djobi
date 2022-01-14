package io.datatok.djobi.plugins.s3.actions;

import io.datatok.djobi.engine.actions.fs.output.FSOutputConfig;
import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionPreChecker;
import io.datatok.djobi.plugins.s3.S3BagKeys;
import io.datatok.djobi.plugins.s3.S3SparkUtils;
import io.datatok.djobi.spark.executor.SparkExecutor;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import javax.inject.Inject;

/**
 * Check bucket exists and is accessible.
 *
 * @todo > still need Spark Hadoop FS :/
 */
public class FSOutputPreCheckerS3 implements ActionPreChecker {

    private static final Logger logger = Logger.getLogger(FSOutputPreCheckerS3.class);

    @Inject
    SparkExecutor executor;

    @Override
    public CheckResult preCheck(Stage stage) throws Exception {
        final FSOutputConfig config = (FSOutputConfig) stage.getParameters();
        final FileSystem fs = S3SparkUtils.getFileSystem(executor.getSparkContext(), config.s3);
        final String bucketName = config.s3.get(S3BagKeys.BUCKET);
        final Path p = new Path("/");

        if (fs.exists(p)) {
            return CheckResult.ok();
        }

        logger.error(String.format("[check:%s] Bucket %s not found or not accessible!", "fs-output-s3", bucketName));

        return CheckResult.error(String.format("Bucket %s not found or not accessible!", bucketName));
    }
}
