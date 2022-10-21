package io.datatok.djobi.plugins.logging.feeders;

import io.datatok.djobi.engine.Job;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class APMFeeder {

    public void append(final Map<String, Object> data, final Map<String, Object> meta) {
        final Map<String, String> apmData = new HashMap<>();

        if (meta.containsKey(Job.META_APM_TRACE_ID)) {
            apmData.put(Job.META_APM_TRACE_ID, (String) meta.get(Job.META_APM_TRACE_ID));
        }

        data.put("apm", apmData);
    }

}
