package io.datatok.djobi.engine.data;

public class DataHolder {

    protected Object data;

    protected String type;

    protected Boolean isFromSpark = false;

    public DataHolder(final Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public String getType() {
        return type;
    }

    public Boolean getFromSpark() {
        return isFromSpark;
    }
}
