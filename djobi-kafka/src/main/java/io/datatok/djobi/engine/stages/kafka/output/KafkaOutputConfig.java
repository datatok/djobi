package io.datatok.djobi.engine.stages.kafka.output;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;

import java.util.List;
import java.util.Map;

public class KafkaOutputConfig extends ActionConfiguration {
    List<String> servers;

    String topic;

    /**
     * @deprecated use kafkaOptions
     */
    Integer batchSize;

    /**
     * @deprecated use kafkaOptions
     */
    Integer bufferSize;

    /**
     * @since v3.1.0
     */
    Map<String, Object> kafkaOptions;

    public KafkaOutputConfig(final Bag stageConfiguration, final Job job, final TemplateUtils templateUtils) {

        super(stageConfiguration, job, templateUtils);

        this.servers = renderList("servers");
        this.topic = render("topic", "");
        this.batchSize = Integer.parseInt(render("batch_size", "16384"));
        this.bufferSize = Integer.parseInt(render("buffer_size", "33554432"));

        this.kafkaOptions = renderObjectsMap("kafka");
    }
}
