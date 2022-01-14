package io.datatok.djobi.plugins.stages.api;

import io.datatok.djobi.engine.flow.ExecutionContext;
import io.datatok.djobi.engine.stage.ActionProvider;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.utils.StageCandidate;
import io.datatok.djobi.plugins.stages.api.ambari.APIAmbariConfigurator;
import io.datatok.djobi.plugins.stages.api.ambari.APIAmbariRunner;
import io.datatok.djobi.plugins.stages.api.ambari.APIAmbariType;
import io.datatok.djobi.plugins.stages.api.yarn.clean.APIYarnCleanConfigurator;
import io.datatok.djobi.plugins.stages.api.yarn.clean.APIYarnCleanRunner;
import io.datatok.djobi.plugins.stages.api.yarn.clean.APIYarnCleanType;

public class APIModule extends ActionProvider {
    @Override
    public void configure() {
        registerConfigurator(APIAmbariType.TYPE, APIAmbariConfigurator.class);
        registerRunner(APIAmbariType.TYPE, APIAmbariRunner.class);

        registerConfigurator(APIYarnCleanType.TYPE, APIYarnCleanConfigurator.class);
        registerRunner(APIYarnCleanType.TYPE, APIYarnCleanRunner.class);
    }

    @Override
    public StageCandidate getGenericActionCandidates(Stage stage, String phase, ExecutionContext executionContext) {
        return null;
    }

    @Override
    public String getName() {
        return "api-hadoop";
    }
}
