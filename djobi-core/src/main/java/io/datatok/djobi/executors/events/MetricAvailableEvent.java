package io.datatok.djobi.executors.events;

import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.event.Event;

import java.util.Map;

public class MetricAvailableEvent extends Event {

    static final public String NAME = "executors-metrics-available";

    private Stage stage;

    private Map<String, Object> metric;

    public MetricAvailableEvent(Stage stage, Map<String, Object> metric) {
        this.stage = stage;
        this.metric = metric;
    }

    public Stage getStage() {
        return stage;
    }

    public Map<String, Object> getMetric() {
        return metric;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
