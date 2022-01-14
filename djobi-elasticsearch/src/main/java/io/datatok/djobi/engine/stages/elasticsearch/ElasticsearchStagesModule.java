package io.datatok.djobi.engine.stages.elasticsearch;

import io.datatok.djobi.engine.flow.ExecutionContext;
import io.datatok.djobi.engine.stage.ActionProvider;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stages.elasticsearch.input.ESSparkInputConfigurator;
import io.datatok.djobi.engine.stages.elasticsearch.input.ESSparkInputRunner;
import io.datatok.djobi.engine.stages.elasticsearch.input.ESSparkInputType;
import io.datatok.djobi.engine.stages.elasticsearch.output.*;
import io.datatok.djobi.engine.utils.StageCandidate;

public class ElasticsearchStagesModule extends ActionProvider {

    static public final String NAME = "org.elasticsearch";

    @Override
    protected void configure() {
        registerConfigurator(ESOutputType.TYPE, ESOutputConfigurator.class);
        registerPreChecker(ESOutputType.TYPE , ESOutputPreChecker.class);
        registerRunner(ESOutputType.TYPE, ESOutputRunner.class);

        registerConfigurator(ESSparkInputType.TYPE , ESSparkInputConfigurator.class);
        registerRunner(ESSparkInputType.TYPE, ESSparkInputRunner.class);

        registerPostChecker(ESOutputType.TYPE, ESOutputPostChecker.class);
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
