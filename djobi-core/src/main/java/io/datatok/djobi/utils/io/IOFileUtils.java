package io.datatok.djobi.utils.io;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IOFileUtils {

    static public String readInClassPath(String path, String def) {
        try {
            return readInClassPath(path);
        } catch(Exception e) {
            return def;
        }
    }

    static public InputStream openInClassPath(String path) throws IOException {
        //Get yaml from resources folder
        ClassLoader classLoader = IOFileUtils.class.getClassLoader();

        InputStream stream = classLoader.getResourceAsStream(path);

        if (stream == null) {
            throw new IOException("Cannot find file " + path);
        }

        return stream;
    }

    static public String readInClassPath(String path) throws Exception {
        //Get yaml from resources folder
        ClassLoader classLoader = IOFileUtils.class.getClassLoader();

        InputStream stream = classLoader.getResourceAsStream(path);

        if (stream == null) {
            throw new Exception("Cannot find file " + path);
        }

        return IOUtils.toString(stream);
    }

    static public String getContent(String fileName) throws IOException {
        //if (new File(fileName).exists()) {
            return new String(Files.readAllBytes(Paths.get(fileName)));
        //}
    }

}
