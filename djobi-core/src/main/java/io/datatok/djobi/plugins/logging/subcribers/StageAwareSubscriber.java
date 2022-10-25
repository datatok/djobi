package io.datatok.djobi.plugins.logging.subcribers;

import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.plugins.logging.LoggerTypes;
import io.datatok.djobi.plugins.logging.feeders.Transformer;
import io.datatok.djobi.plugins.logging.loggers.BaseLogger;
import io.datatok.djobi.plugins.logging.sink.LoggerSinkFactory;
import io.datatok.djobi.utils.MyMapUtils;

import javax.inject.Inject;
import java.util.Map;

abstract class StageAwareSubscriber  extends BaseLogger {

    @Inject
    Transformer feeder;

    public StageAwareSubscriber(LoggerSinkFactory factory) {
        this.sink = factory.get(LoggerTypes.TYPE_STAGES);
    }

    protected Map<String, Object> resolveDocument(final Stage stage) {
        Map<String, Object> data = feeder.transform(stage);

        if (stage.getLog().getException() != null) {
            fillErrorFields(data, stage.getLog().getException());
        }

        return data;
    }
}
