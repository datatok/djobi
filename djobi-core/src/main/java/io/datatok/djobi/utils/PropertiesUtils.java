package io.datatok.djobi.utils;

import java.util.Properties;

public class PropertiesUtils {

    static public Properties build(Object... args) {
        final Properties p = new Properties();

        String key = null;
        for (Object arg : args) {
            if (key == null) {
                key = (String) arg;
            } else {
                p.put(key, arg);
                key = null;
            }
        }

        return p;
    }

}
