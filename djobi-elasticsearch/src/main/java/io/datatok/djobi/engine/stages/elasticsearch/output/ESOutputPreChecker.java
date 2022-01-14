package io.datatok.djobi.engine.stages.elasticsearch.output;

import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionPreChecker;
import io.datatok.djobi.utils.elasticsearch.ElasticsearchUtils;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.io.IOException;

public class ESOutputPreChecker implements ActionPreChecker {

    private static Logger logger = Logger.getLogger(ESOutputPreChecker.class);

    @Inject
    private ElasticsearchUtils esUtils;

    @Override
    public CheckResult preCheck(final Stage stage) {
        final ESOutputConfig config = (ESOutputConfig) stage.getParameters();

        try {
            final String esVersion = esUtils.getElasticsearchServerVersion(config.url);

            if (esVersion.isEmpty()) {
                logger.error(String.format("[check:%s] %s", ESOutputType.TYPE, "version is empty"));
                return CheckResult.error("version is empty");
            }

            logger.info(String.format("[check:%s] Found elasticsearch version %s", ESOutputType.TYPE, esVersion));

        } catch (IOException e) {
            logger.error(String.format("[check:%s] %s", ESOutputType.TYPE, e.getMessage()), e);
            return CheckResult.error(e.getMessage());
        }

        return CheckResult.ok();
    }

}
