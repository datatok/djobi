package io.datatok.djobi.application.plugins;

import io.datatok.djobi.engine.stage.ActionProvider;
import io.datatok.djobi.utils.ClassUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ActionProviderDiscover {

    static Logger logger = Logger.getLogger(ActionProviderDiscover.class);

    public List<ActionProvider> discover() {
        List<ActionProvider> modules = new ArrayList<>();

        List<String> plugins = new ArrayList<String>(){{
            add("io.datatok.djobi.engine.stages.elasticsearch.ElasticsearchStagesModule");
            add("io.datatok.djobi.engine.stages.kafka.KafkaModule");
            add("io.datatok.djobi.user_agent.UserAgentParserModule");
        }};

        for (String plugin : plugins) {
            if (ClassUtils.isClass(plugin)) {
                logger.info("Load plugin - " + plugin);

                try {
                    ActionProvider mod = (ActionProvider) Class.forName(plugin).getConstructor().newInstance();
                    modules.add(mod);
                } catch(ReflectiveOperationException e) {
                    logger.error("Loading plugin \"" + plugin + "\"", e);
                }
            }
        }

        return modules;
    }

}
