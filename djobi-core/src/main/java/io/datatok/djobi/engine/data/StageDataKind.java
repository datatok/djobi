package io.datatok.djobi.engine.data;

public class StageDataKind {

    public String provider;

    public String type;

    public StageDataKind(String provider, String type) {
        this.provider = provider;
        this.type = type;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
