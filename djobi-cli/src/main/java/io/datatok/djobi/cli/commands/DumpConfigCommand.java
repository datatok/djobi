package io.datatok.djobi.cli.commands;

import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValue;
import io.datatok.djobi.cli.utils.CLIOutUtils;
import io.datatok.djobi.configuration.Configuration;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommandLine.Command(name = "env", description = "dump configuration")
public class DumpConfigCommand implements Runnable {

    @Inject
    Configuration config;

    @CommandLine.ParentCommand
    DumpCommand dumpCommand;

    @CommandLine.Parameters(defaultValue = "")
    String filter;

    @Override
    public void run() {
        final ConfigRenderOptions configRenderOptions;
        final ConfigObject configBlock;

        if (dumpCommand.format.equals("json")) {
            configRenderOptions = ConfigRenderOptions.concise();
        } else {
            configRenderOptions = ConfigRenderOptions.defaults();
        }

        if (filter != null && !filter.isEmpty()) {
            configBlock = config.getObject(filter);
        } else {
            configBlock = config.root();
        }

        if (dumpCommand.format.equals("json")) {
            final String out = configBlock.render(configRenderOptions);
            System.out.println(out);
        } else {
            final List<List<String>> tableArgs = new ArrayList<>();

            toTable(configBlock, tableArgs, null);

            System.out.println(CLIOutUtils.table(tableArgs));

            System.out.println();
        }
    }

    private void toTable(final ConfigObject configBlock, final List<List<String>> table, final String prefix) {
        for (Map.Entry<String, ConfigValue> entry : configBlock.entrySet()) {
            final String key =  (prefix == null ? "" : prefix + ".") + entry.getKey();
            if (entry.getValue() instanceof ConfigObject) {
                toTable((ConfigObject) entry.getValue(), table, key);
            } else {
                final String v = entry.getValue().toString().replace("Quoted(\"", "").replace("\")", "");

                if (dumpCommand.applyGrep(key)) {
                    table.add(new ArrayList<String>() {{
                        add(key);
                        add(v);
                    }});
                }
            }
        }
    }
}
