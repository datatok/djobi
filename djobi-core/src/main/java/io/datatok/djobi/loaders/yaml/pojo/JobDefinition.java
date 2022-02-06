package io.datatok.djobi.loaders.yaml.pojo;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.loaders.JobMaterializer;
import io.datatok.djobi.utils.bags.ParameterBag;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class JobDefinition {

    public List<StageDefinition> stages;

    public Map<String, Object> parameters;

    /**
     * @since v5.0.0
     */
    public Map<String, String> labels;

    public Map<String, Map<String, Object>> matrix;

    public Map<String, Map<String, Object>> contexts;

    public String id;

    public final String uid;

    public int order;

    public JobDefinition() {
        this.uid = UUID.randomUUID().toString();
    }

    public JobDefinition setContexts(Map<String, Map<String, Object>> context) {
        this.matrix = context;
        return this;
    }

    public Job getJob(final Pipeline pipeline, final ParameterBag run)
    {
        final Job job = new Job();
        final String id = (String) run.get("_job_id").getValue();

        job
            .setName(id + "?" + JobMaterializer.toID(run.values()))
            .setId(id)
            .setPipeline(pipeline)
            .setOrder(order)
            .setContextKey(run.get("_context_").getValueAsString())
            .setParameters(run)
            .setLabels(labels)
            .setStages(
                stages
                    .stream()
                    .map(StageDefinition::buildStage)
                    .map(s -> s.setJob(job))
                    .collect(Collectors.toList())
            )
        ;

        return job;
    }

}
