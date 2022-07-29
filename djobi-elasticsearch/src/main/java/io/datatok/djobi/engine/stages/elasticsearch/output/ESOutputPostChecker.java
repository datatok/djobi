package io.datatok.djobi.engine.stages.elasticsearch.output;

import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.check.CheckStatus;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionPostChecker;
import io.datatok.djobi.utils.elasticsearch.ElasticsearchUtils;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.io.IOException;

public class ESOutputPostChecker implements ActionPostChecker {

    private static final Logger logger = Logger.getLogger(ESOutputPostChecker.class);

    @Inject
    private ElasticsearchUtils esUtils;

    @Override
    public CheckResult postCheck(Stage stage) throws Exception {
        final ESOutputConfig config = (ESOutputConfig) stage.getParameters();
        final CheckResult result = new CheckResult();
        final String elasticsearchIndex = config.index;

        try {
            logger.debug(String.format("Checking elasticsearch index %s / %s ?q= %s", config.url, elasticsearchIndex, config.clean_query));

            esUtils.refresh(config.url, elasticsearchIndex);

            int count = esUtils.searchCount(config.url, elasticsearchIndex, config.clean_query);

            logger.info(String.format("Found %d documents in elasticsearch index %s at %s", count, elasticsearchIndex, config.url));

            result  .putMeta("index", elasticsearchIndex)
                    .putMeta("query", config.clean_query == null ? "" : config.clean_query)
                    .putMeta("value", count)
                    .setStatus(count == 0 ? CheckStatus.DONE_ERROR : CheckStatus.DONE_OK)
            ;
        } catch (IOException e) {
            logger.error(e.getMessage());

            result  .setStatus(CheckStatus.DONE_ERROR)
                    .putMeta("reason", "Connection error: " + e.getMessage())
            ;
        }

        return result;
    }
}
