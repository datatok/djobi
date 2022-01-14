package io.datatok.djobi.cli;

import com.google.inject.Injector;
import io.datatok.djobi.cli.commands.DjobiCommand;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CommandFactory implements CommandLine.IFactory {

    @Inject
    private Injector injector;

    public CommandFactory() {
        AnsiConsole.systemInstall();
    }

    public void run(final String[] args) {
        CommandLine.run(DjobiCommand.class, this, args);
    }

    @Override
    public <K> K create(Class<K> cls) throws Exception {
        return injector.getInstance(cls);
    }
}
