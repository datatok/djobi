package io.datatok.djobi.cli.commands;

import com.google.inject.Inject;
import io.datatok.djobi.cli.utils.PipelineRequestFactory;
import io.datatok.djobi.cli.utils.CLIUtils;
import io.datatok.djobi.engine.Engine;
import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.engine.PipelineExecutionRequest;
import io.datatok.djobi.engine.events.ErrorEvent;
import io.datatok.djobi.event.EventBus;
import io.datatok.djobi.loaders.yaml.YAMLWorkflowLoader;
import io.datatok.djobi.plugins.report.OutVerbosity;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Map;

@CommandLine.Command(name = "run", description = "run a pipeline")
public class RunPipelineCommand implements Runnable {

    @Inject
    PipelineRequestFactory pipelineRequestFactory;

    @Inject
    YAMLWorkflowLoader pipelineLoader;

    @Inject
    Engine pipelineRunner;

    @Inject
    OutVerbosity outVerbosity;

    @Inject
    CLIUtils cliUtils;

    @Inject
    EventBus eventBus;

    @CommandLine.Option(paramLabel = "args", names = {"-a", "--arg"}, description = "arguments (date, ...)")
    Map<String, String> args;

    @CommandLine.Option(paramLabel = "jobs", names = {"--jobs"}, description = "jobs filter", defaultValue = "")
    String jobs;

    @CommandLine.Option(paramLabel = "run_metas", names = {"-m", "--meta"}, description = "pipeline run meta (for logging)")
    Map<String, String> runMetas;

    @CommandLine.Option(
            paramLabel = "phases",
            names = {"--phases"},
            description = "job phases (default \"configuration,pre_check,run,post_check\")",
            defaultValue = PipelineExecutionRequest.PHASES_FILTER_DEFAULT
    )
    String phases;

    @CommandLine.Parameters(paramLabel = "pipeline", defaultValue = "${DJOBI_PIPELINE}" ,arity = "1..*", description = "the pipeline directory path")
    String pipelinePath;

    @CommandLine.Option(names = { "-v", "--verbose" }, description = "Verbose mode. Helpful for troubleshooting. " +
            "Multiple -v options increase the verbosity.")
    private final boolean[] verbosityOption = new boolean[0];

    @Override
    public void run() {
        Pipeline pipeline = null;

        if (pipelinePath == null || pipelinePath.isEmpty()) {
            cliUtils.printError("pipeline is missing!");
            return ;
        }

        final PipelineExecutionRequest pipelineRequest = pipelineRequestFactory.build(pipelinePath, args, runMetas, jobs, phases, verbosityOption);

        outVerbosity.setVerbosityLevel(pipelineRequest.getVerbosity());

        try {
            pipeline = pipelineLoader.get(pipelineRequest);
        } catch (IOException e) {
            eventBus.trigger(new ErrorEvent(e, e.getMessage()));
        }

        if (pipeline != null) {
            try {
                pipelineRunner.run(pipeline);
            } catch (Exception e) {
                eventBus.trigger(new ErrorEvent(e, e.getMessage()));
            }
        }
    }
}