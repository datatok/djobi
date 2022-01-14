package io.datatok.djobi.plugins.logging.subcribers;

import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.plugins.logging.LoggerTypes;
import io.datatok.djobi.plugins.logging.loggers.BaseLogger;
import io.datatok.djobi.plugins.logging.sink.LoggerSinkFactory;
import io.datatok.djobi.utils.MyMapUtils;

import java.util.Map;

abstract class StageAwareSubscriber  extends BaseLogger {

    public StageAwareSubscriber(LoggerSinkFactory factory) {
        this.sink = factory.get(LoggerTypes.TYPE_STAGES);
    }

    protected Map<String, Object> resolveDocument(final Stage stage) {
        Map<String, Object> data = MyMapUtils.map(
                "job", MyMapUtils.map(
                        "id", stage.getJob().getId(),
                        "uid", stage.getJob().getUid(),
                        "name", stage.getJob().getName()
                ),
                "pipeline", MyMapUtils.map(
                        "uid", stage.getJob().getPipeline().getUid(),
                        "name", stage.getJob().getPipeline().getName()
                ),
                "order", stage.getJob().getStages().indexOf(stage),
                "name", stage.getName(),
                "type", stage.getKind(),
                "config", stage.getParameters(),
                "enabled", stage.getEnabled(),
                "allow_failure", stage.getAllowFailure(),
                "status", stage.getExecutionStatus(),
                "timeline", stage.getTimeline(),
                "logs", stage.getLog().getRawLogs(),
                "metrics", stage.getMetrics(),
                "labels", stage.getLabels(),
                "pre_check", MyMapUtils.map(
                        "status", stage.getPreCheck().getStatus().toString(),
                        "meta", stage.getPreCheck().getMeta()
                ),
                "post_check", MyMapUtils.map(
                        "status", stage.getPostCheck().getStatus().toString(),
                        "meta", stage.getPostCheck().getMeta()
                )
        );

        fillAgentData(data);

        if (stage.getLog().getException() != null) {
            fillErrorFields(data, stage.getLog().getException());
        }

        return data;
    }
}
