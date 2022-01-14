package io.datatok.djobi.user_agent;

import io.datatok.djobi.engine.flow.ExecutionContext;
import io.datatok.djobi.engine.stage.ActionProvider;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.utils.StageCandidate;

public class UserAgentParserModule extends ActionProvider {

    static public final String NAME = "io.djobi.user-agent";

    @Override
    public void configure() {
        registerConfigurator(NAME, UserAgentParserConfigurator.class);
        registerRunner(NAME, UserAgentParserRunner.class);
    }

    @Override
    public StageCandidate getGenericActionCandidates(Stage stage, String phase, ExecutionContext executionContext) {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
