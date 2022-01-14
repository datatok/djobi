package io.datatok.djobi.cli.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import io.datatok.djobi.cli.utils.CLIOutUtils;
import io.datatok.djobi.engine.stage.ActionFactory;
import io.datatok.djobi.plugins.report.Reporter;
import io.datatok.djobi.utils.JSONUtils;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CommandLine.Command(name = "action", description = "dump stage action")
public class DumpActionCommand implements Runnable {

    @CommandLine.ParentCommand
    DumpCommand dumpCommand;

    @Inject
    ActionFactory actionFactory;

    @Inject
    Reporter reporter;

    @Override
    public void run() {

         Map<String, Map<String, String>> data = actionFactory.getAll()
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    phase -> phase
                        .getValue()
                        .entrySet()
                        .stream()
                        .collect(
                            Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().get().getClass().getCanonicalName()
                            )
                        )
                )
            );

        if (dumpCommand.format.equals("json")) {
            try {
                reporter.output(JSONUtils.serialize(data));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else {

            for (Map.Entry<String, Map<String, String>> entryPhase : data.entrySet()) {
                reporter.output("Phase " + entryPhase.getKey() + ":");

                List<List<String>> rows = new ArrayList<>();

                rows.add(Arrays.asList("stage type", "action class"));

                rows.addAll(
                    entryPhase.getValue()
                        .entrySet()
                        .stream()
                        .map(e -> Arrays.asList(e.getKey(), e.getValue()))
                        .collect(Collectors.toList())
                );

                reporter.output(CLIOutUtils.table(rows, true));

                reporter.output("");
            }
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
