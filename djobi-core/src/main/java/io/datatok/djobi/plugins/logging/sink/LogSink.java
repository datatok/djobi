package io.datatok.djobi.plugins.logging.sink;

import java.util.Map;

public interface LogSink {

    String updateOrCreate(final String documentId, final Map<String, Object> data) throws Exception;

    String updateOrCreate(final Map<String, Object> data) throws Exception;

    void updateAtomic(final String documentId, final Map<String, Object> data) throws Exception;

}
