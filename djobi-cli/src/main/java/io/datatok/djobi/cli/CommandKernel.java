package io.datatok.djobi.cli;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.datatok.djobi.cli.commands.RootCommand;
import picocli.CommandLine;

@Singleton
public class CommandKernel {

    @Inject
    private CommandFactory commandFactory;

    @Inject
    private Provider<RootCommand> rootCommandProvider;

    public CommandLine getRootCommand() {
        return new CommandLine(rootCommandProvider.get(), commandFactory);
    }

    public void run(String... args) {
        getRootCommand().execute(args);
    }
}
