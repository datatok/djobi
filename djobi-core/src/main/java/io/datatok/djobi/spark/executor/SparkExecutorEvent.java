package io.datatok.djobi.spark.executor;

import io.datatok.djobi.event.Event;

import java.util.HashMap;
import java.util.Map;

public class SparkExecutorEvent extends Event {

    static public final String NAME = "spark-executor-event";

    /**
     * Specific Spark listener event.
     */
    private String event;

    /**
     * Misc data.
     */
    private Map<String, String> data;

    static public SparkExecutorEvent build(final String event) {
        SparkExecutorEvent i = new SparkExecutorEvent(event, new HashMap<>());

        return i;
    }

    public SparkExecutorEvent(String event) {
        this.event = event;
    }

    public SparkExecutorEvent(String event, Map<String, String> data) {
        this.event = event;
        this.data = data;
    }

    public SparkExecutorEvent addMeta(final String k, final String v) {
        this.data.put(k, v);

        return this;
    }

    public String getEvent() {
        return event;
    }

    public Map<String, String> getData() {
        return data;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
