package io.datatok.djobi.plugins.s3;

import io.datatok.djobi.engine.actions.fs.output.FSOutputType;
import io.datatok.djobi.engine.flow.ExecutionContext;
import io.datatok.djobi.engine.stage.ActionProvider;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.utils.StageCandidate;
import io.datatok.djobi.plugins.s3.actions.FSOutputPostCheckerS3Spark;
import io.datatok.djobi.plugins.s3.actions.FSOutputPreCheckerS3;
import io.datatok.djobi.plugins.s3.actions.FSOutputS3Spark;

public class S3ActionProvider extends ActionProvider {

    static public final String NAME = "s3";

    @Override
    protected void configure() {
        registerRunner(resolveFullname(FSOutputType.TYPE), FSOutputS3Spark.class);
        registerPreChecker(resolveFullname(FSOutputType.TYPE), FSOutputPreCheckerS3.class);
        registerPostChecker(resolveFullname(FSOutputType.TYPE), FSOutputPostCheckerS3Spark.class);

        registerProvider(S3ActionProvider.NAME, this);
    }

    @Override
    public StageCandidate getGenericActionCandidates(Stage stage, String phase, ExecutionContext executionContext) {
        final String stageType = stage.getKind();

        // "fs-output" > handle it if path is S3 like
        if (stageType.equals(FSOutputType.TYPE)) {
            String path = stage.getSpec().getString("path", "");

            if (path.startsWith("s3a:")) {
                return new StageCandidate(resolveFullname(stageType), 255);
            }
        }

        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static String resolveFullname(String name) {
        return NAME + "." + name;
    }
}
