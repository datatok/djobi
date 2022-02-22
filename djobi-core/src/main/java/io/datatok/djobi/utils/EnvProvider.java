package io.datatok.djobi.utils;

import com.google.inject.Singleton;
import com.typesafe.config.ConfigException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class EnvProvider {

    static final public String SEPARATOR = "_";

    static final public String SCOPE_PREFIX_DEFAULT = "DJOBI" + SEPARATOR;

    private final Map<String, String> overrides = new HashMap<>();

    public String get(String key) {
        return System.getenv(key);
    }

    public EnvProvider setScoped(String key, String value) {
        overrides.put(SCOPE_PREFIX_DEFAULT + key, value);
        return this;
    }

    public EnvProvider clearScopedCache() {
        overrides.clear();
        return this;
    }

    public String getScoped(String key) {
        if (overrides.containsKey(key)) {
            return overrides.get(key);
        }
        return System.getenv(SCOPE_PREFIX_DEFAULT + key);
    }

    public Map<String, String> getScopedStartsWith(final String prefix) {
        final Map<String, String> source = new HashMap<>(System.getenv());

        source.putAll(overrides);

        final String prefixAll = SCOPE_PREFIX_DEFAULT + prefix;

        return
            source
                .entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith(prefixAll))
                .collect(Collectors.toMap(e -> envToMapKey(prefixAll, e.getKey()), Map.Entry::getValue));
    }

    static public String envToMapKey(String prefix, String key) {
        return key.substring(prefix.length() + 1).toLowerCase(Locale.ROOT);
    }

    static public Map<String, String> formatKeys(Map<String, String> inMap) {
        return
            inMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> EnvProvider.formatKey(e.getKey()), Map.Entry::getValue));
    }

    static public String formatKey(String variable) {
        final StringBuilder builder = new StringBuilder();
        int underscores = 0;

        for (char c : variable.toLowerCase(Locale.ROOT).toCharArray()) {
            if (c == '_') {
                underscores++;
            } else {
                if (underscores > 0  && underscores < 4) {
                    builder.append(underscoreMappings(underscores));
                } else if (underscores > 3) {
                    builder.append('_');
                }
                underscores = 0;
                builder.append(c);
            }
        }

        if (underscores > 0  && underscores < 4) {
            builder.append(underscoreMappings(underscores));
        } else if (underscores > 3) {
            builder.append('_');
        }

        return builder.toString();
    }

    private static char underscoreMappings(int num) {
        // Rationale on name mangling:
        //
        // Most shells (e.g. bash, sh, etc.) doesn't support any character other
        // than alphanumeric and `_` in environment variables names.
        // In HOCON the default separator is `.` so it is directly translated to a
        // single `_` for convenience; `-` and `_` are less often present in config
        // keys but they have to be representable and the only possible mapping is
        // `_` repeated.
        switch (num) {
            case 1: return '.';
            case 2: return '-';
            case 3: return '_';
            default: return 0;
        }
    }
}
