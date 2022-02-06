package io.datatok.djobi.plugins.logging.config;

public class LoggingSinkConfig {

    private boolean enabled;

    private String storeType;

    private String storeBucket;

    private String storeURL;

    public boolean isEnabled() {
        return enabled;
    }

    public LoggingSinkConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getStoreType() {
        return storeType;
    }

    public LoggingSinkConfig setStoreType(String storeType) {
        this.storeType = storeType;
        return this;
    }

    public String getStoreBucket() {
        return storeBucket;
    }

    public LoggingSinkConfig setStoreBucket(String storeBucket) {
        this.storeBucket = storeBucket;
        return this;
    }

    public String getStoreURL() {
        return storeURL;
    }

    public LoggingSinkConfig setStoreURL(String storeURL) {
        this.storeURL = storeURL;
        return this;
    }
}
