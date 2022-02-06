package io.datatok.djobi.loaders.yaml;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Parameter;
import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.engine.PipelineExecutionRequest;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.executors.ExecutorPool;
import io.datatok.djobi.loaders.JobMaterializer;
import io.datatok.djobi.loaders.yaml.pojo.JobDefinition;
import io.datatok.djobi.loaders.yaml.pojo.PipelineDefinition;
import io.datatok.djobi.utils.ActionArgFactory;
import io.datatok.djobi.utils.PipelineUtils;
import io.datatok.djobi.utils.bags.ParameterBag;
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
public class YAMLPipelineLoader {

    static public final String DEFAULT_CONTEXT = "_default_";

    private static final Logger logger = Logger.getLogger(YAMLPipelineLoader.class);

    private static final Set<String> fileNameCandidates = Set.of("pipeline.yml", "pipeline.yaml", "djobi.yml", "djobi.yaml");
    private static final Set<String> extensionCandidates = Set.of("yml", "yaml");

    @Inject
    private JobMaterializer materializer;

    @Inject
    private ActionArgFactory actionArgFactory;

    @Inject
    private ExecutorPool executorPool;

    public Pipeline get(final PipelineExecutionRequest pipelineRequest) throws IOException {
        final PipelineDefinition pipelineDefinition = getDefinition(pipelineRequest);

        return getImplementation(pipelineRequest, pipelineDefinition);
    }

    static public String resolveJobId(final String job, final ParameterBag run) {
        final String context = run.get("_context_").getValueAsString();
        return context.equals(DEFAULT_CONTEXT) ? job : String.format("%s_%s", job, context);
    }

    /**
     * Retrieve the pipeline definition from user.
     * - Load yaml from yaml system
     * - Load yaml from JAR
     *
     * @return PipelineDefinition
     */
    private PipelineDefinition getDefinition(final PipelineExecutionRequest pipelineRequest) throws IOException {
        final Constructor constructor = new Constructor(PipelineDefinition.class);

        final TypeDescription parameterDescription = new TypeDescription(PipelineDefinition.class);

        parameterDescription.addPropertyParameters("parameters", Parameter.class);

        //constructor.addTypeDescription(parameterDescription);

        final Yaml yaml = new Yaml(constructor);
        final String yamlContent;
        File definitionFile = Paths.get(pipelineRequest.getPipelineDefinitionPath()).toFile();

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
                throw new IOException(String.format("Dont find a valid definition file under %s !", pipelineRequest.getPipelineDefinitionPath()));
            }
        }
        else if (!extensionCandidates.contains(FilenameUtils.getExtension(definitionBaseName)))
        {
            throw new IOException(String.format("Path %s is a wrong file type!", definitionFile.toString()));
        }

        yamlContent = new String(Files.readAllBytes(definitionFile.toPath()));

        final PipelineDefinition pipeline = yaml.loadAs(yamlContent, PipelineDefinition.class);

        pipeline.definitionFile = definitionFile;

        return pipeline;
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

    private Pipeline getImplementation(final PipelineExecutionRequest pipelineRequest, final PipelineDefinition definition) throws IOException {

        final Pipeline pipeline = new Pipeline();
        final List<Job> jobs;

        final ParameterBag pipelineParameters = actionArgFactory.resolve(definition.parameters, pipelineRequest);

        pipeline.setUid(UUID.randomUUID().toString());

        if (definition.jobs != null && definition.jobs.size() > 0) {
            jobs = definition
                    .jobs
                    .entrySet()
                    .stream()
                    //.filter(jobId -> PipelineUtils.acceptJob(pipelineRequest, jobId.getKey()))
                    .flatMap(entry -> {
                        final JobDefinition jobDefinition = entry.getValue();
                        final List<ParameterBag> jobsToRunParameters;
                        final ParameterBag jobParameters = actionArgFactory.resolve(jobDefinition.parameters, pipelineRequest);
                        final Map<String, ParameterBag> jobContextParameters = new HashMap<>();

                        if (jobDefinition.matrix == null) {
                            jobContextParameters.put(DEFAULT_CONTEXT, jobParameters);
                        } else {
                            for (String c : jobDefinition.matrix.keySet()) {
                                jobContextParameters.put(c, ParameterBag.merge(jobParameters, actionArgFactory.resolve(jobDefinition.matrix.get(c), pipelineRequest)));
                            }
                        }

                        try {
                            jobsToRunParameters = materializer.materialize(pipelineParameters, jobContextParameters);
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("JobTaskExpandable", e);
                            return Arrays.stream(new Job[]{});
                        }

                        return resolveJobs(pipelineRequest, pipeline, jobDefinition, entry.getKey(), jobsToRunParameters);
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

        pipeline
            .setPipelineRequest(pipelineRequest)
            .setResourcesDir(definition.definitionFile.getParentFile())
            .setLabels(definition.labels)
            .setExecutor(executor)
            .setJobs(jobs)
        ;

        pipelineRequest
            .setPipeline(pipeline)
        ;

        return pipeline;
    }

    /**
     * Transform job-definitions + parameters matrix into "jobs" to execute.
     *
     * @param pipelineRequest
     * @param pipeline
     * @param jobDefinition
     * @param jobId
     * @param jobsToRunParameters
     * @return
     */
    private Stream<Job> resolveJobs(final PipelineExecutionRequest pipelineRequest, final Pipeline pipeline, final JobDefinition jobDefinition, final String jobId, final List<ParameterBag> jobsToRunParameters) {
        return
            jobsToRunParameters
                .stream()
                .filter(run -> PipelineUtils.acceptJob(pipelineRequest, resolveJobId(jobId, run)))
                .map(run -> run.add("_job_id", resolveJobId(jobId, run)))
                .map(run -> jobDefinition.getJob(pipeline, run))
            ;
    }



}
