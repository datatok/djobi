package io.datatok.djobi.plugins.logging.feeders;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.utils.MyMapUtils;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class JobFeeder {

    public Map<String, Object> feed(Job job) {
        return MyMapUtils.map(
            "id", job.getId(),
            "uid", job.getUid(),
            "pre_check_status", job.getPreCheckStatus().toString(),
            "post_check_status", job.getPostCheckStatus().toString(),
            "args", job.getParameters(),
            "run_status", job.getExecutionStatus().toString(),
            "timeline", job.getTimeline() == null ? new HashMap<>() : job.getTimeline(),
            "labels", job.getLabels()
        );
    }

    public void append(final Map<String, Object> data, final Job job) {
        data.put("job", this.feed(job));
    }

}
