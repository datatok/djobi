package io.datatok.djobi.configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import io.datatok.djobi.utils.EnvProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Singleton
public class EnvVarLoader {

    static final public String PREFIX = "CONFIG";

    @Inject
    EnvProvider envProvider;

    public Config loadEnvVariablesOverrides() {
        final Map<String, String> rawMap = EnvProvider.formatKeys(
            envProvider.getScopedStartsWith(PREFIX)
        );

        return ConfigFactory.parseMap(rawMap, "env variables");
    }




}
