package io.datatok.djobi.utils;

import com.google.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class EnvProviderTest {

    @Inject
    EnvProvider envProvider;

    @Test
    public void testKeyFormatter() {
        Assertions.assertEquals("logger.enabled", EnvProvider.formatKey("LOGGER_ENABLED"));
        Assertions.assertEquals("logger-enabled", EnvProvider.formatKey("LOGGER__ENABLED"));
        Assertions.assertEquals("logger_enabled", EnvProvider.formatKey("LOGGER___ENABLED"));
        Assertions.assertEquals("my-logger.enabled", EnvProvider.formatKey("my__logger_ENABLED"));
    }

    @Test
    public void testEnvMap() {
        envProvider
            .clearScopedCache()
            .setScoped("META_TITLE", "title")
            .setScoped("MTA_NOP", "nop")
            .setScoped("META_THIS_IS_IT", "this is it")
        ;

        Map<String, String> mp = envProvider.getScopedStartsWith("META");

        Assertions.assertEquals(2, mp.size());
        Assertions.assertEquals("title", mp.get("title"));
        Assertions.assertEquals("this is it", mp.get("this_is_it"));
    }

}
