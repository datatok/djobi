package io.datatok.djobi.plugins.logging.config;

import com.google.inject.Inject;
import com.typesafe.config.ConfigFactory;
import io.datatok.djobi.test.MyTestRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MyTestRunner.class)
public class LoggingConfigTest {

    @Inject
    private LoggingConfigFactory loggingConfigFactory;

    @Test
    void testBuild() throws Exception {
        final LoggingConfig config = fromString("sinks { stages { enabled = true, type = elasticsearch } }");

        Assertions.assertNotNull(config);
        Assertions.assertTrue(config.getStageSink().isEnabled());
        Assertions.assertFalse(config.getJobSink().isEnabled());
        Assertions.assertEquals("elasticsearch", config.getStageSink().getStoreType());
    }

    @Test
    void testApplicationConfig() throws Exception {
        final LoggingConfig config = loggingConfigFactory.build();

        Assertions.assertNotNull(config);
        Assertions.assertTrue(config.getStageSink().isEnabled());
        Assertions.assertTrue(config.getJobSink().isEnabled());
        Assertions.assertFalse(config.getMetricSink().isEnabled());
        Assertions.assertEquals("memory", config.getStageSink().getStoreType());
    }

    private LoggingConfig fromString(final String str) {
        return LoggingConfigFactory.build(ConfigFactory.parseString(str));
    }
}
