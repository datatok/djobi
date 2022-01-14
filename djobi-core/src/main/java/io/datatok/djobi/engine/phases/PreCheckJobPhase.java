package io.datatok.djobi.engine.phases;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.check.CheckStatus;
import io.datatok.djobi.engine.events.StagePreCheckDoneEvent;
import io.datatok.djobi.engine.events.StagePreCheckStartEvent;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionPreChecker;
import org.apache.log4j.Logger;

public class PreCheckJobPhase extends AbstractPhase {

    static Logger logger = Logger.getLogger(PreCheckJobPhase.class);

    public void execute(final Job job) {
        if (job.getStages() == null) {
            job.setPreCheckStatus(CheckStatus.DONE_ERROR);

            return ;
        }

        int resFault = 0;
        int resOK = 0;

        for (Stage stage : job.getStages()) {
            if (stage.getEnabled()) {
                if (stage.getPreCheckEnabled()) {
                    ActionPreChecker checker = actionFactory.getPreChecker(stage);

                    if (checker == null) {
                        logger.info(String.format("Stage %s has no pre checker!", stage.getKind()));
                        stage.setPreCheck(CheckResult.no());
                    } else {
                        CheckResult result;

                        this.eventBus.trigger(new StagePreCheckStartEvent(stage));

                        logger.info(String.format("Pre checking stage %s with %s", stage.getKind(), checker.getClass().getCanonicalName()));

                        try {
                            result = checker.preCheck(stage);
                        } catch (Exception e) {
                            logger.error("PreCheck exception", e);
                            result = CheckResult.error(e.getMessage());
                        }

                        stage.setPreCheck(result);

                        switch(result.getStatus()) {
                            case DONE_OK:
                                logger.debug(String.format("[check:stage] [%s:%s] Check ok!", stage.getKind(), stage.getName()));
                                resOK++;
                                break;
                            case DONE_ERROR:
                                final String reason = (String) result.getMeta("reason");
                                logger.warn(String.format("[check:stage] [%s:%s] Check failed: \"%s\"", stage.getKind(), stage.getName(), reason));
                                resFault++;
                                break;
                        }

                        this.eventBus.trigger(new StagePreCheckDoneEvent(stage));
                    }
                } else {
                    stage.setPreCheck(CheckResult.no());
                }
            }
        }

        if (resFault == 0 && resOK > 0) {
            job.setPreCheckStatus(CheckStatus.DONE_OK);
        } else if (resFault > 0) {
            job.setPreCheckStatus(CheckStatus.DONE_ERROR);
        }

        //return resFault == 0;
    }

}
