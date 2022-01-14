package io.datatok.djobi.engine.phases;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionConfigurator;
import org.apache.log4j.Logger;

public class ConfigurePhase extends AbstractPhase {

    static Logger logger = Logger.getLogger(ConfigurePhase.class);

    public void execute(Job job) {
        job.getStages()
                .stream()
                .filter(Stage::getEnabled)
                .forEach(stage -> {
                    final ActionConfigurator configurator = actionFactory.getConfigurator(stage);

                    if (configurator != null) {
                        logger.debug(String.format("Found a configurator for stage %s", stage.getKind()));

                        stage.setParameters(configurator.configure(job, stage));
                    }
                });
    }

}
