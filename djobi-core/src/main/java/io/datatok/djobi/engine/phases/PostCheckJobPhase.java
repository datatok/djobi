package io.datatok.djobi.engine.phases;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.check.CheckStatus;
import io.datatok.djobi.engine.events.ErrorEvent;
import io.datatok.djobi.engine.events.StagePostCheckDoneEvent;
import io.datatok.djobi.engine.events.StagePostCheckStartEvent;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionPostChecker;
import io.datatok.djobi.utils.ExceptionUtils;
import org.apache.log4j.Logger;

public class PostCheckJobPhase extends AbstractPhase {

    static Logger logger = Logger.getLogger(PostCheckJobPhase.class);

    /**
     * Run post-check of output stages.
     * <!> this is optional </!>
     *
     * @param job Job
     */
    public void execute(Job job) {
        int resFault = 0;
        int resOK = 0;

        for (Stage stage : job.getStages()) {
            if (stage.getEnabled() && stage.getPostCheck().getStatus().equals(CheckStatus.TODO)) {
                if (stage.getPostCheckEnabled()) {
                    final ActionPostChecker checker = actionFactory.getPostChecker(stage);

                    if (checker == null) {
                        stage.setPostCheck(CheckResult.no());
                        logger.debug(String.format("Stage %s has no post-checker!", stage.getKind()));
                    } else {
                        CheckResult results;

                        this.eventBus.trigger(new StagePostCheckStartEvent(stage));

                        logger.info(String.format("Post checking stage %s with %s", stage.getKind(), checker.getClass().getCanonicalName()));

                        try {
                            results = checker.postCheck(stage);
                        } catch (Exception e) {
                            final String m = ExceptionUtils.getExceptionMessageChainAsString(e);
                            results = CheckResult.error(m);
                            logger.error("PostCheck exception", e);
                            eventBus.trigger(new ErrorEvent(e, stage));
                        }

                        stage.setPostCheck(results);

                        switch(results.getStatus()) {
                            case DONE_OK:
                                logger.debug(String.format("[%s:%s] Check ok!", stage.getKind(), stage.getName()));
                                resOK++;
                                break;
                            case DONE_ERROR:
                                logger.warn(String.format("[%s:%s] Check failed!", stage.getKind(), stage.getName()));
                                resFault++;
                                break;
                        }
                    }
                } else {
                    stage.setPostCheck(CheckResult.no());
                }

                this.eventBus.trigger(new StagePostCheckDoneEvent(stage));
            }
        }

        if (resFault == 0 && resOK > 0) {
            job.setPostCheckStatus(CheckStatus.DONE_OK);
        } else if (resFault > 0) {
            job.setPostCheckStatus(CheckStatus.DONE_ERROR);
        }
    }

}
