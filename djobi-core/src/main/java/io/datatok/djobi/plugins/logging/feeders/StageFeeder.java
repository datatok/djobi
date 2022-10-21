package io.datatok.djobi.plugins.logging.feeders;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.utils.MyMapUtils;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class StageFeeder {

    public Map<String, Object> feed(Stage stage) {
        return MyMapUtils.map(
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
    }

    public void append(final Map<String, Object> data, final Stage stage) {
        data.put("stage", this.feed(stage));
    }

}
