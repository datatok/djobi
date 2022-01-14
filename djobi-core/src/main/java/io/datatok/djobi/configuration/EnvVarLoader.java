package io.datatok.djobi.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EnvVarLoader {
    private static final String ENV_VAR_PREFIX = "DJOBI_";

    static Config loadEnvVariablesOverrides() {
        final Map<String, String> env = new HashMap<>(System.getenv());
        final Map<String, String> result = new HashMap<>();

        for (String key : env.keySet()) {
            if (key.startsWith(ENV_VAR_PREFIX) && !key.equals(ENV_VAR_PREFIX)) {
                result.put(envVariableAsProperty(key), env.get(key));
            }
        }

        return ConfigFactory.parseMap(result, "env variables");
    }

    static private String envVariableAsProperty(String variable) throws ConfigException {
        StringBuilder builder = new StringBuilder();

        String strippedPrefix = variable.substring(EnvVarLoader.ENV_VAR_PREFIX.length());

        strippedPrefix = strippedPrefix.toLowerCase(Locale.ROOT);

        int underscores = 0;
        for (char c : strippedPrefix.toCharArray()) {
            if (c == '_') {
                underscores++;
            } else {
                if (underscores > 0  && underscores < 4) {
                    builder.append(underscoreMappings(underscores));
                } else if (underscores > 3) {
                    throw new ConfigException.BadPath(variable, "Environment variable contains an un-mapped number of underscores.");
                }
                underscores = 0;
                builder.append(c);
            }
        }

        if (underscores > 0  && underscores < 4) {
            builder.append(underscoreMappings(underscores));
        } else if (underscores > 3) {
            throw new ConfigException.BadPath(variable, "Environment variable contains an un-mapped number of underscores.");
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
