package io.datatok.djobi.utils;

import java.util.*;

public class Bag extends HashMap<String, Object> {

    public Bag() {

    }

    public Bag(Object... args) {
        String key = null;
        for (Object arg : args) {
            if (key == null) {
                key = (String) arg;
            } else {
                put(key, arg);
                key = null;
            }
        }
    }

    public String getString(final String key) {
        final Object v = get(key);

        if (v == null) {
            return null;
        }

        return v instanceof String ? (String) v : v.toString();
    }

    public String getString(final String key, final String def) {
        return (String) getOrDefault(key, def);
    }

    public List<String> getStringList(final String key) {

        final Object buffer = get(key);

        if (buffer instanceof String) {
            return Arrays.asList(((String) buffer).split(","));
        }

        return (List<String>) get(key);
    }

    public Class getClass(final String key) {
        return containsKey(key) ? get(key).getClass() : null;
    }

    public static Bag fromLinkedHashMap(LinkedHashMap<String, Object> source) {
        final Bag b = new Bag();

        b.putAll(source);

        return b;
    }

    public static Bag fromMap(Map<String, Object> source) {
        final Bag b = new Bag();

        b.putAll(source);

        return b;
    }
}
