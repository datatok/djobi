package io.datatok.djobi.engine.data;

import io.datatok.djobi.engine.stage.livecycle.ActionRunner;

import java.util.List;

public class StageData<T> {

    private List<ActionRunner> producers;

    private StageDataKind kind;
    private StageDataKind itemKind;

    private T data;

    public StageData(String kindGroup, String kindType, String itemKindGroup, String itemKindType, T data) {
        this.kind = new StageDataKind(kindGroup, kindType);
        this.itemKind = new StageDataKind(itemKindGroup, itemKindType);

        this.data = data;
    }

    public StageData(String kindGroup, String kindType, T data) {
        this.kind = new StageDataKind(kindGroup, kindType);
        this.data = data;
    }

    public StageData(StageDataKind kind, T data) {
        this.kind = kind;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public List<String> asListOfString() {
        return (List<String>) data;
    }

    public List<ActionRunner> getProducers() {
        return producers;
    }

    public StageDataKind getKind() {
        return kind;
    }

    public StageDataKind getItemKind() {
        return itemKind;
    }
}
