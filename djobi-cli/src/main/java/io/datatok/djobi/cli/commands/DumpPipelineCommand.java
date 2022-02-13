package io.datatok.djobi.cli.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.datatok.djobi.cli.utils.PipelineRequestFactory;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.engine.phases.ActionPhases;
import io.datatok.djobi.engine.phases.StagePhaseMetaData;
import io.datatok.djobi.engine.stage.ActionFactory;
import io.datatok.djobi.loaders.yaml.YAMLPipelineLoader;
import io.datatok.djobi.plugins.report.Reporter;
import io.datatok.djobi.utils.JSONUtils;
import org.apache.commons.lang.StringUtils;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@CommandLine.Command(name = "pipeline", description = "dump a pipeline")
public class DumpPipelineCommand implements Runnable {

    @Inject
    YAMLPipelineLoader pipelineLoader;

    @Inject
    Reporter reporter;

    @Inject
    ActionFactory actionFactory;

    @Inject
    PipelineRequestFactory pipelineRequestFactory;

    @CommandLine.ParentCommand
    DumpCommand dumpCommand;

    @CommandLine.Option(paramLabel = "jobs", names = {"--jobs"}, description = "jobs filter", defaultValue = "")
    public String jobs;

    @CommandLine.Option(paramLabel = "args", names = {"-A", "--args"}, description = "arguments (date, ...)")
    public Map<String, String> args;

    @CommandLine.Parameters(paramLabel = "pipeline", arity = "1..*", description = "the pipeline directory path")
    public String pipelinePath;

    @Override
    public void run() {
        Pipeline pipeline = null;

        AnsiConsole.systemInstall();

        try {
            pipeline = pipelineLoader.get(
                    pipelineRequestFactory.build(pipelinePath, args, null, jobs, "", null)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (pipeline == null) {
            System.err.println("pipeline not found!");
        } else {
            final Map<String, ?> res = pipeline.toHash();

            if (dumpCommand.format.equals("json")) {
                try {
                    reporter.output(JSONUtils.serialize(res));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else {
                reporter.printSummary(pipeline);

                for (final Job job : pipeline.getJobs()) {
                    // Find stage -> action
                    job.getStages().forEach(stage -> {
                        stage.addPhase(new StagePhaseMetaData(ActionPhases.CONFIGURE, actionFactory.getConfigurator(stage)));
                        stage.addPhase(new StagePhaseMetaData(ActionPhases.PRE_CHECK, actionFactory.getPreChecker(stage)));
                        stage.addPhase(new StagePhaseMetaData(ActionPhases.RUN, actionFactory.getRunner(stage)));
                        stage.addPhase(new StagePhaseMetaData(ActionPhases.POST_CHECK, actionFactory.getPostChecker(stage)));
                    });

                    reporter.output(String.format("@|blue %s|@", job.getName()));
                    reporter.output(StringUtils.rightPad("", 100, "-"));

                    reporter.printSummary(job, true);
                }
            }
        }
    }


}
