package io.datatok.djobi.engine.actions.net.checkers.listing;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.datatok.djobi.utils.MyMapUtils;
import io.datatok.djobi.utils.http.Http;
import io.datatok.djobi.utils.http.HttpResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class HttpListing {

    static final public class Record {
        public String file;
        public Date date;
        public long size;

        public Record(String file, Date date, long size) {
            this.file = file;
            this.date = date;
            this.size = size;
        }

        public String getFile() {
            return file;
        }
    }

    static private Logger logger = Logger.getLogger(HttpListing.class);

    @Inject
    private Http http;

    private String fetchUrl;

    private String fetchFormat;

    private String fetchPrefix;

    private Map<String, Record> rawData;


    void init(Config configParent) {
        Config defaults = ConfigFactory.parseMap(MyMapUtils.mapString("format", "csv", "prefix", ""));
        Config config = configParent.withFallback(defaults);

        this.fetchUrl = config.getString("url");
        this.fetchFormat = config.getString("format");
        this.fetchPrefix = config.getString("prefix");
    }

    Record getFile(final String file) throws IOException {
        if (rawData == null) {
            fetchData();
        }

        return rawData.get(resolveFileName(file));
    }

    private String resolveFileName(String file) {
        if (file.startsWith(fetchPrefix)) {
            file = file.replace(fetchPrefix, "");
        }

        file = StringUtils.strip(file, "/");

        file = file.trim();

        return file;
    }

    private void fetchData() throws IOException {
        if (this.fetchUrl == null || this.fetchUrl.isEmpty()) {
            throw new IOException("Fetch URL is empty or null!");
        }

        logger.info(String.format("Fetching files listing at \"%s\"", this.fetchUrl));

        HttpResponse response = http.get(this.fetchUrl).execute();

        if (response.statusCode() == 200) {
            final CSVFormat csvFileFormat = CSVFormat.DEFAULT
                    .withRecordSeparator("\n")
                    .withHeader("file", "date", "size")
                    .withDelimiter(';')
                    .withEscape('\\')
            ;

            final StringReader reader = new StringReader(response.raw());
            final CSVParser parser = csvFileFormat.parse(reader);
            final SimpleDateFormat dateParser = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSSSSSSSS zzzz");

            this.rawData = parser.getRecords()
                    .stream()
                    .map(item -> {
                        try {
                            return new Record(resolveFileName(item.get("file")), dateParser.parse(item.get("date")), Long.parseLong(item.get("size")));
                        } catch (ParseException e) {
                            logger.error(String.format("Error parsing date \"%s\" (%s)!", item.get("date"), e.getMessage()));
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Record::getFile, Function.identity()))
            ;

        } else {
            throw new IOException("Server returns a wrong status code: " + response.statusCode());
        }
    }

}
