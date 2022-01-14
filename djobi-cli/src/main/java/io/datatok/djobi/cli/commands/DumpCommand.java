package io.datatok.djobi.cli.commands;

import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "dump", description = "dump stuff", subcommands = {
        DumpConfigCommand.class,
        DumpPipelineCommand.class,
        DumpActionCommand.class
})
public class DumpCommand implements Runnable {

    @CommandLine.Option(names = {"-f", "--format"}, defaultValue = "plain")
    String format;

    @CommandLine.Option(names = {"-g", "--grep"}, defaultValue = "", split = ",")
    List<String> grep;

    @Override
    public void run() {

    }

    public boolean applyGrep(final String text) {
        if (grep != null && !grep.isEmpty()) {
            for (final String g : grep) {
                if (!text.equals(g) && !text.contains(g)) {
                    return false;
                }
            }

            return true;
        }

        return true;
    }
}
