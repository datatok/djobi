package io.datatok.djobi.plugins.logging.feeders;

import io.datatok.djobi.engine.Workflow;
import io.datatok.djobi.utils.MyMapUtils;

import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class WorkflowFeeder {

    public Map<String, Object> feed(final Workflow workflow) {
        return MyMapUtils.map(
            "uid", workflow.getUid(),
            "name", workflow.getName(),
            "labels", workflow.getLabels()
        );
    }

    public void append(final Map<String, Object> data, final Workflow workflow) {
        data.put("workflow", this.feed(workflow));
        data.put("meta", workflow.getExecutionRequest().getMetaDataLabels());
    }

}
