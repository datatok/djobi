package io.datatok.djobi.engine.data;

import java.util.List;

/**
 * Represent serialized data per line, such as "CSV" , "JSON-L"
 */
public class StageDataListString extends StageData<List<String>> {

    /**
     * json / csv
     */
    private String serializer;

    public StageDataListString(String serializer, List<String> data) {
        super(new StageDataKind(BasicDataKind.GROUP, BasicDataKind.TYPE_LIST), data);

        this.serializer = serializer;
    }

    public StageDataListString(List<String> data) {
        super(new StageDataKind(BasicDataKind.GROUP, BasicDataKind.TYPE_LIST), data);
    }

    public String getSerializer() {
        return serializer;
    }
}
