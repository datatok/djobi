package io.datatok.djobi.loaders.yaml.pojo;

import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.utils.Bag;

import java.util.Map;

public class StageDefinition {

    public String name;

    public String kind = "transform";

    public Boolean enabled = true;

    /** @since v2.0.0 **/
    public Boolean check = true;

    public Boolean allowFailure = false;

    public Bag spec;

    /**
     * @since v3.9.0
     *
     * Hold meta information (input, output format, description..)
     */
    public Bag meta;

    /**
     * @since v5.0.0
     */
    public Map<String, String> labels;

    /**
     * @since v3.10.0
     */
    public String condition;

    public Stage buildStage(final String name) {
        return new Stage()
                .setAllowFailure(allowFailure)
                .setCondition(condition)
                .setName(name)
                .setEnabled(enabled)
                .setPreCheckEnabled(check)
                .setPostCheckEnabled(check)
                .setSpec(spec)
                .setKind(kind)
                .setLabels(labels)
        ;
    }
}
