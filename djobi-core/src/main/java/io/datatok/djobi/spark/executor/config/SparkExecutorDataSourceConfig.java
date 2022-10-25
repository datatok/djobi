package io.datatok.djobi.spark.executor.config;

import com.typesafe.config.Optional;

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
    private String name;

    @Optional
    private String type;

    @Optional
    private String format;

    @Optional
    private String path;

    @Optional
    private String sql;

    public SparkExecutorDataSourceConfig() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
