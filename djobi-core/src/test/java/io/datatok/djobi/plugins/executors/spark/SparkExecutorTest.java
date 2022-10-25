package io.datatok.djobi.plugins.executors.spark;

import com.google.inject.Inject;
import io.datatok.djobi.spark.executor.SparkExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class SparkExecutorTest {

    @Inject
    SparkExecutor sparkExecutor;

    @Test
    void testTableSetup() throws IOException {
        sparkExecutor.connect();

        long count = sparkExecutor.getSQLContext().sql("SELECT * FROM utils_country").count();

        Assertions.assertEquals(248, count);
    }

}
