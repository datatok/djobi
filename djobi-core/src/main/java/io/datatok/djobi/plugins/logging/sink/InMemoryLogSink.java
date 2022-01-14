package io.datatok.djobi.plugins.logging.sink;

import io.datatok.djobi.utils.JSONUtils;
import io.datatok.djobi.utils.MyMapUtils;

import java.util.HashMap;
import java.util.Map;

public class InMemoryLogSink implements LogSink {

    private Map<String, Map> documents;

    public InMemoryLogSink() {
        this.documents = new HashMap<>();
    }

    @Override
    public String updateOrCreate(String documentId, Map data) throws Exception {
        documents.put(documentId, convertObject(data));

        return documentId;
    }

    @Override
    public String updateOrCreate(Map data) throws Exception {
        return null;
    }

    @Override
    public void updateAtomic(String documentId, Map data) throws Exception {
        documents.put(documentId, MyMapUtils.deepMerge(getDocument(documentId) == null ? new HashMap() : getDocument(documentId), convertObject(data)));
    }

    public Map getDocuments() {
        return documents;
    }

    public Map getDocument(String documentId) {
        return documents.get(documentId);
    }

    private Map convertObject(Map data) {
        try {
            return JSONUtils.parse(JSONUtils.serialize(data), Map.class);
        } catch(Exception e) {
            e.printStackTrace();
            return data;
        }
    }
}
