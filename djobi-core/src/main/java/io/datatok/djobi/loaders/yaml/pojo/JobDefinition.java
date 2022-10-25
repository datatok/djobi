package io.datatok.djobi.loaders.yaml.pojo;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Workflow;
import io.datatok.djobi.loaders.matrix.MatrixGenerator;
import io.datatok.djobi.utils.bags.ParameterBag;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class JobDefinition {

    /**
     * Stages definition
     */
    public List<StageDefinition> stages;

    /**
     * Parameters definition
     */
    public Map<String, Object> parameters;

    /**
     * Labels definition
     */
    public Map<String, String> labels;

    /**
     * Define a matrix to execute job as variant.
     */
    public Map<String, Map<String, Object>> matrix;

    /**
     * Raw name from YAML
     */
    public String name;

    /**
     * Real name template
     */
    public String id;

    public final String uid;

    public int order;

    public JobDefinition() {
        this.uid = UUID.randomUUID().toString();
    }

    public Job toJobImpl(final Workflow workflow, final ParameterBag run)
    {
        final Job job = new Job();
        final String id = (String) run.get("_job_id").getValue();

        job
            .setName(id + "?" + MatrixGenerator.toUUID(run.values()))
            .setId(id)
            .setWorkflow(workflow)
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
