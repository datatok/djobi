package io.datatok.djobi.engine.stage.livecycle;

import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.stage.Stage;

/**
 * @since v2.2.8 (rename on v3.1.0)
 */
public interface ActionPreChecker extends Action {

    CheckResult preCheck(final Stage stage) throws Exception;

}