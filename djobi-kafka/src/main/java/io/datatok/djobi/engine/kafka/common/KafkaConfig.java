package io.datatok.djobi.engine.kafka.common;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.MyMapUtils;
import io.datatok.djobi.utils.templating.TemplateUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KafkaConfig extends ActionConfiguration {
    public List<String> endpoints;

    /**
     * @since v5.0.0
     */
    public final KafkaTopicConfig topic = new KafkaTopicConfig();

    /**
     * @since v3.1.0
     */
    public Map<String, String> kafkaOptions = new HashMap<>();

    public KafkaConfig(final Bag stageConfiguration, final Job job, final TemplateUtils templateUtils) {

        super(stageConfiguration, job, templateUtils);

        this.endpoints = renderList("endpoints");

        if (stageConfiguration.containsKey("topic")) {
            if (stageConfiguration.get("topic").getClass().equals(String.class)) {
                this.topic.name = render("topic", "");
            } else {
                final Bag topic = Bag.fromLinkedHashMap((LinkedHashMap<String, Object>) stageConfiguration.get("topic"));

                this.topic.name = render(topic, "name", null);
                this.topic.create = topic.get("create").equals("true") || topic.get("create").equals("yes");
                this.topic.partitions = (int) topic.getOrDefault("partitions", 1);
                this.topic.replicationFactor = Short.parseShort(topic.getOrDefault("replications", 1).toString());
            }
        }

        if (stageConfiguration.containsKey("options")) {
            Map<String, Object> buffer = renderObjectsMap("options");
            Map<String, String> bufferStr = MyMapUtils.valuesToString(buffer);

            this.kafkaOptions.putAll(bufferStr);
        }
    }
}
