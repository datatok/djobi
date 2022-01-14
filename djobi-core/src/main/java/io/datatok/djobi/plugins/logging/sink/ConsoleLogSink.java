package io.datatok.djobi.plugins.logging.sink;

import java.util.Map;

public class ConsoleLogSink implements LogSink {
    @Override
    public String updateOrCreate(String documentId, Map data) throws Exception {
        return null;
    }

    @Override
    public String updateOrCreate(Map data) throws Exception {
        return null;
    }

    @Override
    public void updateAtomic(String documentId, Map data) throws Exception {

    }
}
