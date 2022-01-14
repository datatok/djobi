package io.datatok.djobi.executors;

import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.Dumpable;

import java.io.IOException;
import java.util.Map;

public interface Executor extends Dumpable {

    /**
     * Get the executor type.
     *
     * @return string
     */
    String getType();

    /**
     * Connect the executor.
     */
    void connect() throws Exception;

    /**
     * Set the stage.
     *
     * @param stage
     */
    void setCurrentStage(final Stage stage);

    /**
     * Get meta, useful for logging.
     * @return Map<String, String>
     */
    Map<String, String> getMeta() throws IOException;

    /**
     * DIC.
     * @param service
     * @return Object
     */
    Object get(String service) throws Exception;

    /**
     * Get the executor or application title/name.
     *
     * @return String
     */
    String getTitle();

    /**
     * Get logs data.
     *
     * @return Map<String, String>
     */
    Map<String, String> getLogs();

    void configure(Bag conf);

}
