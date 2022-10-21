package io.datatok.djobi.cli.commands;

import picocli.CommandLine;

@CommandLine.Command(
    name = "djobi",
    sortOptions = false,
    header = {
    "@|green Djobi, run data job as a cool pipeline, by Datatok!|@",
    ""},
    //descriptionHeading = "@|bold %nDescription|@:%n",
    description = {
            "",
            "Execute pipeline.", },
    optionListHeading = "@|bold %nOptions|@:%n",
    footer = {
            "",
            "@|cyan If you would like to contribute or report an issue|@",
            "@|cyan go to github: https://github.com/datatok/djobi|@",
            "",
            "@|cyan This project was created by Thomas Decaux (@ebuildy) |@",
            ""},
    subcommands = {DumpCommand.class, ExecuteWorkflowCommand.class}
)
public class RootCommand implements Runnable {

    public void run() {
        //reporter.output("Djobi version %s", app.getVersion());

        //new CommandLine(this, commandFactory).usage(System.out);
    }
}
