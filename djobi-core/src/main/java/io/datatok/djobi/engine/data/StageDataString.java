package io.datatok.djobi.engine.data;

/**
 * Hold a simple string
 */
public class StageDataString extends StageData<String> {

    public StageDataString(String data) {
        super(new StageDataKind(BasicDataKind.GROUP, BasicDataKind.TYPE_STRING), data);
    }
}
