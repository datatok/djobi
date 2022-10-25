package io.datatok.djobi.engine.stages.kafka.output;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.kafka.common.KafkaConfig;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;

public class KafkaOutputConfig extends KafkaConfig {
    public KafkaOutputConfig(Bag stageConfiguration, Job job, TemplateUtils templateUtils) {
        super(stageConfiguration, job, templateUtils);
    }
}
