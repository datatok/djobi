package io.datatok.djobi.plugins.logging.feeders;

import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.utils.MyMapUtils;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;

@Singleton
public class ExecutorFeeder {

    public Map<String, Object> feed(final Executor executor) {
        try {
            return MyMapUtils.map(executor.getType(), executor.getMeta());
        } catch (IOException e) {
            e.printStackTrace();

            return MyMapUtils.map("error", e.getMessage());
        }
    }

    public void append(final Map<String, Object> data, final Executor executor) {
        data.put("executor", this.feed(executor));
    }

}
