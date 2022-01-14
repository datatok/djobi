package io.datatok.djobi.engine.data;

import java.util.List;

public class StageDataList<T> extends StageData<List<T>> {

    public StageDataList(List<T> data) {
        super(new StageDataKind(BasicDataKind.GROUP, BasicDataKind.TYPE_LIST), data);
    }
}
