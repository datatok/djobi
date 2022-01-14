package io.datatok.djobi.spark.actions.output;

import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionPostChecker;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import java.util.List;

public class SparkHiveOutputPostChecker implements ActionPostChecker {

    static String REASON_TABLE_NOT_FOUND = "table not found";
    static String REASON_QUERY_EMPTY = "query results nothing";

    @Override
    public CheckResult postCheck(Stage stage) throws Exception {
        final SparkHiveOutputConfig config = (SparkHiveOutputConfig) stage.getParameters();
        final SQLContext sqlEngine = (SQLContext) stage.getExecutor().get("sql_context");

        if (config.table != null) {
            if (ArrayUtils.indexOf(sqlEngine.tableNames(), config.table) == -1) {
                return ActionPostChecker.error(REASON_TABLE_NOT_FOUND);
            }
        }

        sqlEngine.sql("REFRESH TABLE " + config.table);

        if (config.checkQuery != null && !config.checkQuery.isEmpty()) {
            final List<Row> rows = sqlEngine.sql(config.checkQuery).collectAsList();

            if (rows.isEmpty()) {
                return ActionPostChecker.error(REASON_QUERY_EMPTY);
            } else {
                long l = rows.get(0).getLong(0);

                if (l > 0) {
                    return CheckResult.ok(
                        "display", l + " rows",
                        "value", l,
                        "unit", "row"
                    );
                } else {
                    return ActionPostChecker.error(REASON_QUERY_EMPTY);
                }
            }
        }

        return ActionPostChecker.unknown();
    }
}
