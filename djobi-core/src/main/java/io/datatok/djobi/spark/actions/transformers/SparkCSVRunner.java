package io.datatok.djobi.spark.actions.transformers;

import io.datatok.djobi.engine.actions.csv.CSVFilterConfig;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.data.StageDataString;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.exceptions.DataException;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.data.SparkDataframe;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

/**
 * @todo    This should not collect any data, but transform Dataset<?> ==> Dataset<String>
 *          like ds.toJSON() do.
 */
public class SparkCSVRunner implements ActionRunner {

    static public final String TYPE = "csv";

    public ActionRunResult run(final Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception
    {
        final CSVFilterConfig config = (CSVFilterConfig) stage.getParameters();
        final Writer writer = new StringWriter();
        final CSVFormat csvFileFormat = CSVFormat.DEFAULT
                .withRecordSeparator("\n")
                .withDelimiter(config.delimiter)
                .withEscape('\\')
                .withQuoteMode(config.quoteMode)
        ;
        final CSVPrinter csvPrinter = new CSVPrinter(writer, csvFileFormat);

        if (contextData instanceof SparkDataframe) {
            final Dataset<Row> df = ((SparkDataframe) contextData).getData();

            csvPrinter.printRecord(Arrays.asList(df.schema().fieldNames()));

            for (Row row : df.collectAsList()) {
                for (int i = 0; i < row.size(); i++) {
                    csvPrinter.print(row.get(i) == null ? "" : row.get(i).toString());
                }

                csvPrinter.println();
            }

        } else {
            throw new DataException(stage, "data must be DataFrame!");
        }

        /*if (((Job) job).getPipeline().getPipelineRequest().debug())
        {

        }*/
/*
        try {
            Files.write(Paths.get("test.csv"), writer.toString().getBytes("UTF-8"),  StandardOpenOption.CREATE_NEW);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        //System.out.println(writer.toString());

        return ActionRunResult.success(new StageDataString(writer.toString()));
    }
}
