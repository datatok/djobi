package io.datatok.djobi.configuration;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.datatok.djobi.test.MyTestRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static com.github.stefanbirkner.systemlambda.SystemLambda.*;

@ExtendWith(MyTestRunner.class)
class EnvVarLoaderTest {

    @Inject
    EnvVarLoader envVarLoader;

    @Test
    void testPreCheck() throws Exception {
        withEnvironmentVariable("first", "first value")
            .and("DJOBI_", "second value")
            .and("DJOBI_LOG", "second value")
            .and("DJOBI_CONFIG_LOGGER_ENABLED", "false")
            .execute(() -> {
                Config config = envVarLoader.loadEnvVariablesOverrides();

                Assertions.assertFalse(config.hasPath("toto"));
                Assertions.assertFalse(config.hasPath("log"));
                Assertions.assertTrue(config.hasPath("logger.enabled"));
            });
    }

}