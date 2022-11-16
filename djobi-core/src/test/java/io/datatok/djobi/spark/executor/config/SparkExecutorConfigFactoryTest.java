package io.datatok.djobi.spark.executor.config;

import com.typesafe.config.ConfigFactory;
import io.datatok.djobi.executors.ExecutorPool;
import io.datatok.djobi.spark.executor.SparkExecutor;
import org.apache.spark.SparkContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.io.IOException;
import java.util.Map;

class SparkExecutorConfigFactoryTest {

    @Inject
    SparkExecutorConfigFactory configFactory;

    @Inject
    protected ExecutorPool executorPool;

    @Test
    void create() {
        SparkExecutorConfig config = configFactory.create(ConfigFactory.parseString("""
            defaults {
                master = "local[1]"
                appName = "djobi-test"
            }
            
            webHistoryUrlForJob = "http://localhost:4040"
            
            extraDataSources = [
              {
                name: "utils_country"
                type: "table"
                format: "parquet"
                path: ${?projectRoot}"/dev/data/utils_country"
              }
            ]
        """).resolve());

        Assertions.assertEquals("djobi-test", config.getDefaults().getAppName());
    }

    @Test
    void createWithConfig() {
        SparkExecutorConfig config = configFactory.create(ConfigFactory.parseString("""
            conf {
              spark {
                driver {
                  allowMultipleContexts = false
                  memory = "256M"
                  host = "localhost"
                }
    
                es {
                  scroll.size = 100
                  nodes.wan.only= true
                  mapping.date.rich = false
                  net {
                    #ssl = true
                    ssl.cert.allow.self.signed = true
                  }
                }
    
                sql {
                  shuffle.partitions = 10
                }
              }
            }
            """).resolve());

        final Map<String, String> flattenConfig = config.getConfFlatten();

        Assertions.assertEquals("10", flattenConfig.get("spark.sql.shuffle.partitions"));
        Assertions.assertEquals("100", flattenConfig.get("spark.es.scroll.size"));
        Assertions.assertEquals("true", flattenConfig.get("spark.es.nodes.wan.only"));
    }

    @Test
    void shouldConfigureSparkContext() throws IOException {
        final SparkExecutor executor = (SparkExecutor) executorPool.get(SparkExecutor.TYPE);

        executor.connect();

        final SparkContext context = executor.getSparkContext();

        Assertions.assertEquals("djobi", context.appName());

        Assertions.assertEquals("true", context.getConf().get("spark.es.nodes.wan.only"));
    }
}
