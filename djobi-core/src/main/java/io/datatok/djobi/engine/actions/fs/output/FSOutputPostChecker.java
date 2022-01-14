package io.datatok.djobi.engine.actions.fs.output;

import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionPostChecker;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FSOutputPostChecker implements ActionPostChecker {

    static private long SIZE_MIN = 50;

    static private String REASON_NOT_FOUND = "File not found";

    static private String REASON_TOO_SMALL = "File exists, but too small";

    @Override
    public CheckResult postCheck(Stage stage) throws Exception {
        final FSOutputConfig config = (FSOutputConfig) stage.getParameters();

        Path p = Paths.get(config.path.replace("file://", ""));

        if (Files.exists(p)) {
            long l = Files.size(p);

            if (l < SIZE_MIN) {
                return ActionPostChecker.error(REASON_TOO_SMALL);
            }

            return CheckResult.ok(
                "display", FileUtils.byteCountToDisplaySize(l),
                "value", l,
                "unit", "byte"
            );
        }

        return ActionPostChecker.error(REASON_NOT_FOUND);
    }

}
