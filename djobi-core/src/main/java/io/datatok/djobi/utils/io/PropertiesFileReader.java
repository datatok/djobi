package io.datatok.djobi.utils.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesFileReader {

    static public Map<String, String> read(final String releaseNoteFilePath) throws IOException {
        final Properties buffer = new Properties();
        final ClassLoader classLoader = PropertiesFileReader.class.getClassLoader();
        final InputStream inputStream = classLoader.getResourceAsStream(releaseNoteFilePath);

        if (inputStream != null) {
            buffer.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + releaseNoteFilePath + "' not found in the classpath");
        }

        final Map<String, String> out = new HashMap<>();

        for (Map.Entry<Object, Object> entry : buffer.entrySet()) {
            out.put(entry.getKey().toString(), entry.getValue().toString());
        }

        return out;
    }

}
