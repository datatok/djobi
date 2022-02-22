package io.datatok.djobi.cli;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.datatok.djobi.cli.utils.CLIUtils;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;

@Singleton
public class CommandFactory implements CommandLine.IFactory {

    @Inject
    private Injector injector;

    @Inject
    private CLIUtils cliUtils;

    @Override
    public <K> K create(Class<K> cls) throws Exception {
        if (cls.equals(Map.class) || cls.equals(List.class)) {
            return CommandLine.defaultFactory().create(cls);
        }
        return injector.getInstance(cls);
    }
}
