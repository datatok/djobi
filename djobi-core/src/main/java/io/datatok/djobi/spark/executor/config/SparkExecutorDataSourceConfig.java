package io.datatok.djobi.spark.executor.config;

/**
 * Represent a data source:
 * data {
 *           utils_country {
 *             type: "table"
 *             format: "parquet"
 *             path: ${?projectRoot}"/dev/data/utils_country"
 *             columns: {
 *               test: "toto"
 *             }
 *           }
 *         }
 */
public class SparkExecutorDataSourceConfig {

    public String type;
    public String format;
    public String path;
    public String sql;
}
