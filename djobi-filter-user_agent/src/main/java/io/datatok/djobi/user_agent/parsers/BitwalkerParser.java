package io.datatok.djobi.user_agent.parsers;

import eu.bitwalker.useragentutils.UserAgent;
import eu.bitwalker.useragentutils.Version;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use Bitwalker UA parser.
 */
final public class BitwalkerParser implements Serializable {

    final List<String> parserFields = Arrays.asList(
        "fullname",
        "name",
        "vendor",
        "type",
        "major",
        "minor",
        "version",
        "os",
        "os_name",
        "os_vendor",
        "device"
    );

    public void setup(final SQLContext sqlContext, final List<String> selectedFields) {

        /**
         * Order field according schema.
         */
        final List<String> sortedSelectedFields = selectedFields
                .stream()
                .sorted(Comparator.comparingInt(parserFields::indexOf))
                .collect(Collectors.toList());

        /**
         * If input string is not valid.
         */
        final Object[] emptyResults = selectedFields
                .stream()
                .map(f -> "")
                .toArray();

        final UDF1<String, Row> udf = new UDF1<String, Row>() {
            @Override
            public Row call(String str) throws Exception {
                if (str == null || str.trim().isEmpty() || str.length() < 10) {
                    return RowFactory.create(emptyResults);
                }

                UserAgent userAgent = UserAgent.parseUserAgentString(str);

                final List<String> res = new ArrayList<>();

                if (selectedFields.contains("fullname")) {
                    res.add(userAgent.getBrowser().getName());
                }

                if (selectedFields.contains("name")) {
                    res.add(userAgent.getBrowser().getGroup().getName());
                }

                if (selectedFields.contains("vendor")) {
                    res.add(userAgent.getBrowser().getManufacturer().getName());
                }

                if (selectedFields.contains("type")) {
                    res.add(userAgent.getBrowser().getBrowserType().getName());
                }

                if (selectedFields.contains("major") || selectedFields.contains("minor") || selectedFields.contains("version")) {
                    final Version version = userAgent.getBrowser().getVersion(str);

                    if (selectedFields.contains("major")) {
                        res.add(version == null ? "" : version.getMajorVersion());
                    }

                    if (selectedFields.contains("minor")) {
                        res.add(version == null ? "" : version.getMinorVersion());
                    }

                    if (selectedFields.contains("version")) {
                        res.add(version == null ? "" : version.getVersion());
                    }
                }

                if (selectedFields.contains("os")) {
                    res.add(userAgent.getOperatingSystem().getName());
                }

                if (selectedFields.contains("os_name")) {
                    res.add(userAgent.getOperatingSystem().getGroup().getName());
                }

                if (selectedFields.contains("os_vendor")) {
                    res.add(userAgent.getOperatingSystem().getManufacturer().getName());
                }

                if (selectedFields.contains("device")) {
                    res.add(userAgent.getOperatingSystem().getDeviceType().getName());
                }

                return RowFactory.create(res.toArray());
            }
        };

        sqlContext.udf().register("parse_user_agent", udf, DataTypes.createStructType(
            sortedSelectedFields.stream().map(f -> DataTypes.createStructField(f, DataTypes.StringType, true)).collect(Collectors.toList()))
        );
    }
}
