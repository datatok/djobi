package io.datatok.djobi.configuration;

import com.typesafe.config.*;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Configuration implements Config {

    private Config config;

    public Configuration(Config config) {
        Configuration.this.config = config;
    }

    /**
     * @since v2.2.1
     * @param path
     * @return
     */
    public boolean isPathEnabled(String path) {
        return config.hasPath(path) && (!config.hasPath(path + ".enabled") || config.getBoolean(path + ".enabled"));
    }

    public String getStringWithDefault(String path, String def) {
        return hasPath(path) ? getString(path) : def;
    }

    public Config withFallback(ConfigMergeable other) {
        return config.withFallback(other);
    }

    public ConfigObject root() {
        return config.root();
    }

    public ConfigOrigin origin() {
        return config.origin();
    }

    public Config resolve() {
        return config.resolve();
    }

    public Config resolve(ConfigResolveOptions options) {
        return config.resolve(options);
    }

    public boolean isResolved() {
        return config.isResolved();
    }

    public Config resolveWith(Config source) {
        return config.resolveWith(source);
    }

    public Config resolveWith(Config source, ConfigResolveOptions options) {
        return config.resolveWith(source, options);
    }

    public void checkValid(Config reference, String... restrictToPaths) {
        config.checkValid(reference, restrictToPaths);
    }

    public boolean hasPath(String path) {
        return config.hasPath(path);
    }

    public boolean hasPathOrNull(String path) {
        return config.hasPathOrNull(path);
    }

    public boolean isEmpty() {
        return config.isEmpty();
    }

    public Set<Map.Entry<String, ConfigValue>> entrySet() {
        return config.entrySet();
    }

    public boolean getIsNull(String path) {
        return config.getIsNull(path);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public Number getNumber(String path) {
        return config.getNumber(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public long getLong(String path) {
        return config.getLong(path);
    }

    public double getDouble(String path) {
        return config.getDouble(path);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public <T extends Enum<T>> T getEnum(Class<T> enumClass, String path) {
        return config.getEnum(enumClass, path);
    }

    public ConfigObject getObject(String path) {
        return config.getObject(path);
    }

    public Config getConfig(String path) {
        return config.getConfig(path);
    }

    public Object getAnyRef(String path) {
        return config.getAnyRef(path);
    }

    public ConfigValue getValue(String path) {
        return config.getValue(path);
    }

    public Long getBytes(String path) {
        return config.getBytes(path);
    }

    public ConfigMemorySize getMemorySize(String path) {
        return config.getMemorySize(path);
    }

    public Long getMilliseconds(String path) {
        return config.getMilliseconds(path);
    }

    public Long getNanoseconds(String path) {
        return config.getNanoseconds(path);
    }

    public long getDuration(String path, TimeUnit unit) {
        return config.getDuration(path, unit);
    }

    public Duration getDuration(String path) {
        return config.getDuration(path);
    }

    public Period getPeriod(String path) {
        return config.getPeriod(path);
    }

    public TemporalAmount getTemporal(String path) {
        return config.getTemporal(path);
    }

    public ConfigList getList(String path) {
        return config.getList(path);
    }

    public List<Boolean> getBooleanList(String path) {
        return config.getBooleanList(path);
    }

    public List<Number> getNumberList(String path) {
        return config.getNumberList(path);
    }

    public List<Integer> getIntList(String path) {
        return config.getIntList(path);
    }

    public List<Long> getLongList(String path) {
        return config.getLongList(path);
    }

    public List<Double> getDoubleList(String path) {
        return config.getDoubleList(path);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public <T extends Enum<T>> List<T> getEnumList(Class<T> enumClass, String path) {
        return config.getEnumList(enumClass, path);
    }

    public List<? extends ConfigObject> getObjectList(String path) {
        return config.getObjectList(path);
    }

    public List<? extends Config> getConfigList(String path) {
        return config.getConfigList(path);
    }

    public List<? extends Object> getAnyRefList(String path) {
        return config.getAnyRefList(path);
    }

    public List<Long> getBytesList(String path) {
        return config.getBytesList(path);
    }

    public List<ConfigMemorySize> getMemorySizeList(String path) {
        return config.getMemorySizeList(path);
    }

    public List<Long> getMillisecondsList(String path) {
        return config.getMillisecondsList(path);
    }

    public List<Long> getNanosecondsList(String path) {
        return config.getNanosecondsList(path);
    }

    public List<Long> getDurationList(String path, TimeUnit unit) {
        return config.getDurationList(path, unit);
    }

    public List<Duration> getDurationList(String path) {
        return config.getDurationList(path);
    }

    public Config withOnlyPath(String path) {
        return config.withOnlyPath(path);
    }

    public Config withoutPath(String path) {
        return config.withoutPath(path);
    }

    public Config atPath(String path) {
        return config.atPath(path);
    }

    public Config atKey(String key) {
        return config.atKey(key);
    }

    public Config withValue(String path, ConfigValue value) {
        return config.withValue(path, value);
    }

}
