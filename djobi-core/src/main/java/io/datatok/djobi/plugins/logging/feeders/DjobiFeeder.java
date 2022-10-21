package io.datatok.djobi.plugins.logging.feeders;

import io.datatok.djobi.application.ApplicationData;
import io.datatok.djobi.utils.MyMapUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class DjobiFeeder {

    @Inject
    private ApplicationData runData;

    public void append(final Map<String, Object> data) {
        data.put("agent", MyMapUtils.mapString(
                "name", "djobi",
                "version",  runData.getVersion(),
                "type", "application"
        ));

        data.put("ecs", MyMapUtils.mapString(
                "version",  "1.0.0"
        ));
    }

}
