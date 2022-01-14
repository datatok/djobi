package io.datatok.djobi.executors;

import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.MyMapUtils;

import java.util.HashMap;
import java.util.Map;

public class LocalExecutor implements Executor {

    final static public String TYPE = "local";

    @Override
    public String getType() {
        return TYPE;
    }


    @Override
    public void connect() throws Exception {

    }

    @Override
    public void configure(Bag conf) {

    }

    @Override
    public void setCurrentStage(Stage stage) {

    }

    @Override
    public Map<String, String> getMeta() {
        return null;
    }

    @Override
    public Object get(String service) throws Exception {
        switch (service) {
            case "hdfs":
                return null;
            case "sql_context":
                return null;
            case "context":
                return null;
        }

        throw new Exception("Service not found!");
    }

    @Override
    public String getTitle() {
        return "dummy dude !";
    }

    @Override
    public Map<String, String> getLogs() {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> toHash() {
        return MyMapUtils.map(
                "type", TYPE
        );
    }
}
