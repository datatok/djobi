package io.datatok.djobi.utils;

import org.apache.commons.collections.map.AbstractMapDecorator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MyMapUtils {

    @SafeVarargs
    static public <K, V> Map<K, V> merge(final Map<K, V>... args) {
        final Map<K, V> buffer = new HashMap<>();

        for (final Map<K, V> m : args) {
            if (m != null) {
                buffer.putAll(m);
            }
        }

        return buffer;
    }

    static public Map deepMerge(Map original, Map newMap) {
        for (Object key : newMap.keySet()) {
            if (newMap.get(key) instanceof Map && original.get(key) instanceof Map) {
                Map originalChild = (Map) original.get(key);
                Map newChild = (Map) newMap.get(key);
                original.put(key, deepMerge(originalChild, newChild));
            } else if (newMap.get(key) instanceof List && original.get(key) instanceof List) {
                List originalChild = (List) original.get(key);
                List newChild = (List) newMap.get(key);
                for (Object each : newChild) {
                    if (!originalChild.contains(each)) {
                        originalChild.add(each);
                    }
                }
            } else {
                original.put(key, newMap.get(key));
            }
        }
        return original;
    }

    public static <K, V> Map<K, V> map(final Object... args) {
        final Map<K, V> res = new HashMap<>();
        K key = null;

        for (Object arg : args) {
            if (key == null) {
                key = (K) arg;
            } else {
                res.put(key, (V) arg);
                key = null;
            }
        }
        return res;
    }

    public static Map<String, String> mapString(final String... args) {
        final Map<String, String> res = new HashMap<>();
        String key = null;

        for (Object arg : args) {
            if (key == null) {
                key = (String) arg;
            } else {
                res.put(key, (String) arg);
                key = null;
            }
        }
        return res;
    }

    public static Map wrapByMissingKeyException(final Map map) {
        return new AbstractMapDecorator(map) {
            @Override
            public boolean containsKey(Object key) {
                if (!super.containsKey(key)) {
                    throw new IllegalStateException("Missing key: '" + key +"'");
                }
                return super.containsKey(key);
            }

            @Override
            public Object get(Object key) {
                if (!super.containsKey(key)) {
                    throw new IllegalStateException("Missing key: '" + key +"'");
                }
                return super.get(key);
            }
        };
    }

    public static Object browse(Map map, String key) throws IOException {
        final String[] fields = key.split("\\.");
        Object buffer = map;

        for (final String f : fields) {
            if (buffer instanceof Map) {
                buffer = ((Map<String, Object>) buffer).get(f);
            } else if (buffer instanceof List) {
                buffer = ((List) buffer).get(Integer.parseInt(f));
            } else {
                throw new IOException("not a map!");
            }
        }

        return buffer;
    }

    public static <V> Map<String, V> flattenKeys(Map<String, Object> input) {
        return flattenKeys(input, ".", "");
    }

    public static <V> Map<String, V> flattenKeys(Map<String, Object> input, String separator) {
        return flattenKeys(input, separator, "");
    }

    @SuppressWarnings("unchecked")
    public static <V> Map<String, V> flattenKeys(Map<String, Object> input, String separator, String key) {
        final Map<String, V> res = new HashMap<>();

        for (Map.Entry<String, Object> e : input.entrySet()) {
            final String newKey = key.trim().isEmpty() ? e.getKey() : (key + separator + e.getKey());

            if (e.getValue() instanceof Map) {
                res.putAll(flattenKeys((Map<String, Object>) e.getValue(), separator, newKey));  // recursive call
            } else {
                res.put(newKey, (V) e.getValue());
            }
        }
        return res;
    }

    public static Map<String, String> valuesToString(Map<String, Object> source) {
        return source.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (String)e.getValue()));
    }
}
