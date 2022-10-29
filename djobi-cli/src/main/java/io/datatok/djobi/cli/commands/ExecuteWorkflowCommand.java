package io.datatok.djobi.cli.commands;

import com.google.inject.Inject;
import io.datatok.djobi.cli.utils.WorkflowRequestFactory;
import io.datatok.djobi.cli.utils.CLIUtils;
import io.datatok.djobi.engine.Engine;
import io.datatok.djobi.engine.Workflow;
import io.datatok.djobi.engine.ExecutionRequest;
import io.datatok.djobi.engine.events.ErrorEvent;
import io.datatok.djobi.event.EventBus;
import io.datatok.djobi.loaders.yaml.YAMLWorkflowLoader;
import io.datatok.djobi.plugins.report.OutVerbosity;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Map;

@CommandLine.Command(name = "run", description = "run a pipeline")
public class ExecuteWorkflowCommand implements Runnable {

    @Inject
    WorkflowRequestFactory workflowRequestFactory;

    @Inject
    YAMLWorkflowLoader loader;

    @Inject
    Engine engine;

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
            defaultValue = ExecutionRequest.PHASES_FILTER_DEFAULT
    )
    String phases;

    @CommandLine.Parameters(paramLabel = "workflow", defaultValue = "${DJOBI_PIPELINE}" ,arity = "1..*", description = "workflow definition URL (ex: YAML file path)")
    String workflowDefinitionURL;

    @CommandLine.Option(names = { "-v", "--verbosity" }, description = "Set the stdout report verbosity (\"quiet\"|\"normal\"|\"verbose\"|\"alicia\")")
    String verbosityOption;

    @Override
    public void run() {
        Workflow workflow = null;

        if (workflowDefinitionURL == null || workflowDefinitionURL.isEmpty()) {
            cliUtils.printError("pipeline is missing!");
            return ;
        }

        final ExecutionRequest pipelineRequest = workflowRequestFactory.build(workflowDefinitionURL, args, runMetas, jobs, phases, verbosityOption);

        outVerbosity.setVerbosityLevel(pipelineRequest.getVerbosity());

        try {
            workflow = loader.get(pipelineRequest);
        } catch (IOException e) {
            eventBus.trigger(new ErrorEvent(e, e.getMessage()));
        }

        if (workflow != null) {
            try {
                engine.run(workflow);
            } catch (Exception e) {
                eventBus.trigger(new ErrorEvent(e, e.getMessage()));
            }
        }
    }
}
