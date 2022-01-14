package io.datatok.djobi.engine.stage.livecycle;

import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.check.CheckStatus;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.utils.MyMapUtils;

/**
 * Check produced data is ok.
 *
 * @since v2.3.0
 */
public interface ActionPostChecker extends Action {

    CheckResult postCheck(final Stage stage) throws Exception;

    static CheckResult unknown() {
        return new CheckResult(CheckStatus.DONE_UNKNOWN);
    }

    static CheckResult error(String reason) {
        return new CheckResult(CheckStatus.DONE_ERROR, MyMapUtils.map("reason", reason));
    }
}
