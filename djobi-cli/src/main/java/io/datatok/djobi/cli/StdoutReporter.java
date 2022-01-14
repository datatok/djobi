package io.datatok.djobi.cli;

import io.datatok.djobi.cli.utils.CLIOutUtils;
import io.datatok.djobi.cli.utils.CLIUtils;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.engine.phases.StagePhaseMetaData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.plugins.report.Reporter;
import io.datatok.djobi.utils.ClassUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Singleton;
import java.io.PrintStream;
import java.util.Map;
import java.util.stream.Collectors;

import static org.fusesource.jansi.Ansi.ansi;

@Singleton
public class StdoutReporter implements Reporter {

    private PrintStream stdoutPrintStream = System.out;

    @Override
    public void output(String format, Object... args) {
        CLIUtils.output(format, args);
    }

    @Override
    public String success(String text) {
        return CLIOutUtils.success(text);
    }

    @Override
    public String error(String text) {
        return CLIOutUtils.error(text);
    }

    @Override
    public PrintStream getPrintStream() {
        return stdoutPrintStream;
    }

    @Override
    public void setPrintStream(PrintStream printStream) {
        this.stdoutPrintStream = printStream;
    }

    @Override
    public void printSummary(final Pipeline pipeline) {
        print( ansi().eraseScreen().render("@|red %s|@", pipeline.getName()).toString() );
        print(StringUtils.rightPad("", 100, "="));

        print( ansi().render("%10s: %10s [%s]", "jobs", pipeline.getJobs().size(), pipeline.getJobs().stream().map(Job::getName).collect(Collectors.joining( "," ))).toString() );
        print( ansi().render("%10s: %10s", "path", pipeline.getResourcesDir().getAbsolutePath()).toString() );

        print();
    }

    @Override
    public void printSummary(final Job job) {
        printSummary(job, false);
    }

    @Override
    public void printSummary(final Job job, boolean displayParameters) {
        int stageBoxWidth = 20;
        int stageDetailsWidth = 100;
        boolean displayTimeline = true;

        print(CLIOutUtils.table(job.getParameters()));

        print();

        for (final Stage stage : job.getStages()) {

            displayTimeline = stage != job.getStages().get(job.getStages().size() - 1);

            print(CLIOutUtils.border(stageBoxWidth - 1) + "  " + CLIOutUtils.border(stageDetailsWidth - 1));
            print(CLIOutUtils.cellCenter(stageBoxWidth, stage.getName()) + "--" + CLIOutUtils.cellCenter(stageDetailsWidth, stage.getKind()));
            print(CLIOutUtils.border(stageBoxWidth - 1) + "  " + CLIOutUtils.border(stageDetailsWidth - 1));

            if (displayParameters && stage.getSpec() != null) {
                for (Map.Entry<String, Object> entry : stage.getSpec().entrySet()) {
                    print(CLIOutUtils.cellCenterWithoutBorder(stageBoxWidth, displayTimeline ? "|" : "") + "  " + CLIOutUtils.cell(stageDetailsWidth, String.format("* %-15s %-20s", entry.getKey(), entry.getValue().toString())));
                }
            }

            if (displayParameters && stage.getParameters() != null) {
                for (Map.Entry<String, String> entry : ClassUtils.getClassFieldValues(stage.getParameters()).entrySet()) {
                    print(CLIOutUtils.cellCenterWithoutBorder(stageBoxWidth, displayTimeline ? "|" : "") + "  " + CLIOutUtils.cell(stageDetailsWidth, String.format("* %-15s %-20s", entry.getKey(), entry.getValue())));
                }
            }

            print(CLIOutUtils.cellCenterWithoutBorder(stageBoxWidth, displayTimeline ? "|" : "") + "  " + CLIOutUtils.border(stageDetailsWidth - 1));

            for (StagePhaseMetaData metaData : stage.getPhases()) {
                print(CLIOutUtils.cellCenterWithoutBorder(stageBoxWidth, displayTimeline ? "|" : "") + "  " + CLIOutUtils.cell(stageDetailsWidth, String.format("* %-15s %-20s", metaData.getPhase(), metaData.getAction() == null ? "" : metaData.getAction().getClass().getCanonicalName())));
            }

            print(CLIOutUtils.cellCenterWithoutBorder(stageBoxWidth, displayTimeline ? "|" : "") + "  " + CLIOutUtils.border(stageDetailsWidth - 1));

            print(StringUtils.center(displayTimeline ? "|" : "", stageBoxWidth));
        }

        print();
    }

    private void print(final String text) {
        stdoutPrintStream.println(text);
    }

    private void print() {
        stdoutPrintStream.println();
    }
}
