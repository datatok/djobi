package io.datatok.djobi.cli.commands;

import com.google.inject.Inject;
import io.datatok.djobi.cli.utils.CLIUtils;
import io.datatok.djobi.engine.Engine;
import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.engine.PipelineExecutionRequest;
import io.datatok.djobi.loaders.yaml.YAMLPipelineLoader;
import io.datatok.djobi.plugins.report.OutVerbosity;
import io.datatok.djobi.plugins.report.VerbosityLevel;
import io.datatok.djobi.utils.MetaUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@CommandLine.Command(name = "run", description = "run a pipeline")
public class RunPipelineCommand implements Runnable {

    @Inject
    YAMLPipelineLoader pipelineLoader;

    @Inject
    Engine pipelineRunner;

    @Inject
    MetaUtils metaUtils;

    @Inject
    OutVerbosity outVerbosity;

    @CommandLine.Option(paramLabel = "args", names = {"-A", "--args"}, description = "arguments (date, ...)")
    Map<String, String> args;

    @CommandLine.Option(paramLabel = "jobs", names = {"--jobs"}, description = "jobs filter", defaultValue = "")
    String jobs;

    @CommandLine.Option(paramLabel = "run_metas", names = {"-M", "--meta"}, description = "pipeline run meta (for logging)")
    Map<String, String> runMetas;

    @CommandLine.Option(
            paramLabel = "phases",
            names = {"--phases"},
            description = "job phases (default \"configuration,pre_check,run,post_check\")",
            defaultValue = PipelineExecutionRequest.PHASES_FILTER_DEFAULT
    )
    String phases;

    @CommandLine.Parameters(paramLabel = "pipeline", arity = "1..*", description = "the pipeline directory path")
    String pipelinePath;

    @CommandLine.Option(names = { "-v", "--verbose" }, description = "Verbose mode. Helpful for troubleshooting. " +
            "Multiple -v options increase the verbosity.")
    private boolean[] verbose = new boolean[0];

    @Override
    public void run() {
        Pipeline pipeline = null;

        if (pipelinePath != null && !pipelinePath.isEmpty()) {

            final PipelineExecutionRequest pipelineRequest = new PipelineExecutionRequest(args);

            pipelineRequest
                .setPipelineDefinitionPath(pipelinePath)
                .setJobsFilter(Arrays.asList(this.jobs.split(",")))
                .setJobPhases(Arrays.asList(this.phases.split(",")))
                .setMeta(metaUtils.clean(runMetas))
                .setVerbosity(getVerbosity())
            ;

            outVerbosity.setVerbosityLevel(pipelineRequest.getVerbosity());

            try {
                pipeline = pipelineLoader.get(pipelineRequest);
                pipelineRequest.setPipeline(pipeline);
            } catch (IOException e) {
                CLIUtils.printError(e.getMessage());
                e.printStackTrace();
            }

            if (pipeline != null) {
                try {
                    pipelineRunner.run(pipelineRequest);
                } catch (Exception e) {
                    CLIUtils.printError(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private VerbosityLevel getVerbosity() {
        int l = verbose.length;

        if (l < 1) {
            return VerbosityLevel.NORMAL;
        }

        if (l < 2) {
            return VerbosityLevel.VERBOSE;
        }

        if (l < 3) {
            return VerbosityLevel.VERY_VERBOSE;
        }

        if (l < 4) {
            return VerbosityLevel.VERY_VERY_VERBOSE;
        }

        return VerbosityLevel.ALICIA;
    }
}
