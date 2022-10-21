package io.datatok.djobi.loaders.yaml;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Parameter;
import io.datatok.djobi.engine.Workflow;
import io.datatok.djobi.engine.ExecutionRequest;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.executors.ExecutorPool;
import io.datatok.djobi.loaders.matrix.MatrixGenerator;
import io.datatok.djobi.loaders.yaml.pojo.JobDefinition;
import io.datatok.djobi.loaders.yaml.pojo.WorkflowDefinition;
import io.datatok.djobi.utils.ActionArgFactory;
import io.datatok.djobi.utils.MyMapUtils;
import io.datatok.djobi.loaders.utils.WKJobFilter;
import io.datatok.djobi.utils.bags.ParameterBag;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class YAMLWorkflowLoader {

    static public final String JOB_DEFAULT_ID_TEMPLATE = "{{ name }}-{{ matrix._context_ }}";
    static public final String DEFAULT_CONTEXT = "_default_";

    private static final Logger logger = Logger.getLogger(YAMLWorkflowLoader.class);

    private static final Set<String> fileNameCandidates = Set.of("pipeline.yml", "pipeline.yaml", "djobi.yml", "djobi.yaml");
    private static final Set<String> extensionCandidates = Set.of("yml", "yaml");

    @Inject
    private MatrixGenerator materializer;

    @Inject
    private ActionArgFactory actionArgFactory;

    @Inject
    private ExecutorPool executorPool;

    @Inject
    private TemplateUtils templateUtils;

    public Workflow get(final ExecutionRequest pipelineRequest) throws IOException {
        final WorkflowDefinition pipelineDefinition = getDefinition(pipelineRequest);

        return getImplementation(pipelineRequest, pipelineDefinition);
    }

    /**
     * Return the real job ID, based on job name + current matrix elements.
     */
    public String resolveJobId(final JobDefinition definition, final ParameterBag run) {
        final String context = run.get("_context_").getValueAsString();

        if (context.equals(DEFAULT_CONTEXT)) {
            return definition.name;
        }

        String template = JOB_DEFAULT_ID_TEMPLATE;

        if (definition.id != null && !definition.id.isEmpty()) {
            template = definition.id;
        }

        return templateUtils.renderTemplate(template, MyMapUtils.map(
           "name", definition.name,
           "matrix", run
        ));
    }

    /**
     * Retrieve the pipeline definition from user.
     * - Load yaml from yaml system
     * - Load yaml from JAR
     *
     * @return PipelineDefinition
     */
    private WorkflowDefinition getDefinition(final ExecutionRequest pipelineRequest) throws IOException {
        final Constructor constructor = new Constructor(WorkflowDefinition.class);

        final TypeDescription parameterDescription = new TypeDescription(WorkflowDefinition.class);

        parameterDescription.addPropertyParameters("parameters", Parameter.class);

        //constructor.addTypeDescription(parameterDescription);

        final Yaml yaml = new Yaml(constructor);
        final String yamlContent;
        File definitionFile = Paths.get(pipelineRequest.getDefinitionURI()).toFile();

        if (!definitionFile.exists())
        {
            throw new IOException(String.format("Path %s do not exist!", definitionFile.toString()));
        }

        logger.debug("Loading " + definitionFile);

        final String definitionBaseName = definitionFile.getName();

        yaml.setBeanAccess(BeanAccess.FIELD);

        if (definitionFile.isDirectory())
        {
            definitionFile = searchInDirectory(definitionFile);

            if (definitionFile == null)
            {
                throw new IOException(String.format("Dont find a valid definition file under %s !", pipelineRequest.getDefinitionURI()));
            }
        }
        else if (!extensionCandidates.contains(FilenameUtils.getExtension(definitionBaseName)))
        {
            throw new IOException(String.format("Path %s is a wrong file type!", definitionFile.toString()));
        }

        yamlContent = new String(Files.readAllBytes(definitionFile.toPath()));

        final WorkflowDefinition workflow = yaml.loadAs(yamlContent, WorkflowDefinition.class);

        workflow.definitionFile = definitionFile;

        /** Not mandatory field?
        for (JobDefinition jobDefinition : workflow.jobs) {
            if (jobDefinition.matrix.keySet().size() > 0 && (
                    jobDefinition.id == null || jobDefinition.id.isEmpty()
            )) {
                throw new IOException(String.format("job [%s] is missing a [id] field ", jobDefinition.name));
            }
        }*/

        return workflow;
    }

    /**
     * Search good file under directory.
     *
     * @since v5.0.0
     * @param dir Where to search
     * @return File found or null
     */
    private File searchInDirectory(File dir)
    {
        for (String candidate : fileNameCandidates)
        {
            final File fCandidate = new File(dir.getAbsolutePath().trim().concat("/").concat(candidate));

            if (fCandidate.exists())
            {
                return fCandidate;
            }
        }

        return null;
    }

    private Workflow getImplementation(final ExecutionRequest pipelineRequest, final WorkflowDefinition definition) throws IOException {

        final Workflow workflow = new Workflow();
        final List<Job> jobs;

        final ParameterBag pipelineParameters = actionArgFactory.resolve(definition.parameters, pipelineRequest);

        workflow.setUid(UUID.randomUUID().toString());

        if (definition.jobs != null && definition.jobs.size() > 0) {
            jobs = definition
                    .jobs
                    .stream()
                    //.filter(jobId -> PipelineUtils.acceptJob(pipelineRequest, jobId.getKey()))
                    .flatMap(jobDefinition -> {
                        final List<ParameterBag> jobsToRunParameters;
                        final ParameterBag jobParameters = actionArgFactory.resolve(jobDefinition.parameters, pipelineRequest);
                        final Map<String, ParameterBag> jobContextParameters = new HashMap<>();

                        if (jobDefinition.matrix == null) {
                            jobContextParameters.put(DEFAULT_CONTEXT, jobParameters);
                        } else {
                            for (String c :  jobDefinition.matrix.keySet()) {
                                jobContextParameters.put(c, ParameterBag.merge(
                                    jobParameters,
                                    actionArgFactory.resolve(
                                        jobDefinition.matrix.get(c),
                                        pipelineRequest
                                    )
                                ));
                            }
                        }

                        try {
                            jobsToRunParameters = materializer.generate(pipelineParameters, jobContextParameters);
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("JobTaskExpandable", e);
                            return Arrays.stream(new Job[]{});
                        }

                        return resolveJobs(pipelineRequest, workflow, jobDefinition, jobsToRunParameters);
                    })
                    .collect(Collectors.toList());
        } else {
            jobs = new ArrayList<>();
        }

        /**
         * @todo refactor: Executor must be created for 1 pipeline
         */

        final Executor executor;

        if (definition.executor != null && definition.executor.type != null) {
            executor = executorPool.get(definition.executor.type);
        } else {
            executor = executorPool.getDefault();
        }

        if (definition.executor != null && definition.executor.spec != null) {
            executor.configure(definition.executor.spec);
        }

        workflow
            .setExecutionRequest(pipelineRequest)
            .setResourcesDir(definition.definitionFile.getParentFile())
            .setLabels(definition.labels)
            .setExecutor(executor)
            .setJobs(jobs)
        ;

        return workflow;
    }

    /**
     * Transform job-definitions + parameters matrix into "jobs" to execute.
     */
    private Stream<Job> resolveJobs(final ExecutionRequest pipelineRequest, final Workflow workflow, final JobDefinition definition, final List<ParameterBag> jobsToRunParameters) {
        return
            jobsToRunParameters
                .stream()
                .map(run -> run.add("_job_id", resolveJobId(definition, run)))
                .map(run -> definition.toJobImpl(workflow, run))
                .filter(job -> WKJobFilter.accept(pipelineRequest, job))
            ;
    }

}
