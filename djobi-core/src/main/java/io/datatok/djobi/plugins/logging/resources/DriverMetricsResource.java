package io.datatok.djobi.plugins.logging.resources;

import io.datatok.djobi.utils.MyMapUtils;

import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class DriverMetricsResource {

    public final Map<String, Object> getMap() {
        final Runtime runtime = Runtime.getRuntime();

        return MyMapUtils.map(
                "memory_heap", runtime.totalMemory(),
                "memory_heap_max", runtime.maxMemory(),
                "memory_heap_free", runtime.freeMemory()
        );
    }

}
