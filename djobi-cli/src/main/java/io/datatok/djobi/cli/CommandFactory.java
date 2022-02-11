package io.datatok.djobi.cli;

import com.google.inject.Injector;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.Map;

@Singleton
public class CommandFactory implements CommandLine.IFactory {

    @Inject
    private Injector injector;

    public CommandFactory() {
        AnsiConsole.systemInstall();
    }

    @Override
    public <K> K create(Class<K> cls) throws Exception {
        if (cls.equals(Map.class)) {
            return CommandLine.defaultFactory().create(cls);
        }
        return injector.getInstance(cls);
    }
}
